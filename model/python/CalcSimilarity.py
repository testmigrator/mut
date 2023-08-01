import sqlite3
import sys

import numpy as np
from munkres import Munkres
from nltk.corpus import wordnet as wn

"""
为了优化性能，需要将中间结果做缓存，避免重复计算：
缓存1. 每个单词对应的近义词集合
缓存2. 两个单词之间的相似度
缓存3. 两个短语之间的相似度

java调用python每次都是初始化dict 所以用redis存储
wordSysnDict = dict()
wordSimDict = dict()
phraseSimDict = dict()

"""
wordSysnDict = dict()
wordSimDict = dict()
phraseSimDict = dict()
tokenRelatedSimThreshold = 0.7
stringSimThreshold = 0.9995
methodDocumentSimThreshold = 0.995


def getVectorDict(vectorDictFilepath: str):
    """
    获取向量字典
    key：单词
    value：单词对应的向量
    :param apiVectorDictFilepath: 词向量文件
    :return:
    """
    apiVectorDict = dict()
    with open(vectorDictFilepath, 'r') as file:
        for line in file.readlines():
            kv = line.strip().split('|')
            list1 = kv[1].split(',')
            list2 = list()
            for i in list1:
                list2.append(float(i))
            arr = np.array(list2)
            apiVectorDict[str(kv[0])] = arr
    return apiVectorDict


def loadApiDataFromSQLLite(taskId: str, dbFilepath: str):
    conn = sqlite3.connect(dbFilepath)
    # api_basic表结构:
    # id, task_id, filepath, class_name, api_name,
    # type, method_word_sequence, token_sequence, token_vector
    cursor = conn.cursor()
    sourceApiQuerySql = "select * from api_basic where task_id = " + taskId + " and type = 0"
    cursor.execute(sourceApiQuerySql)
    sourceApis = cursor.fetchall()

    targetApiQuerySql = "select * from api_basic where task_id = " + taskId + " and type = 1"
    cursor.execute(targetApiQuerySql)
    targetApis = cursor.fetchall()

    return sourceApis, targetApis


def cos_sim(a, b):
    if not (np.any(a)) or not (np.any(b)):
        return 0
    a_norm = np.linalg.norm(a)
    b_norm = np.linalg.norm(b)
    cos = np.dot(a, b) / (a_norm * b_norm)
    return cos


"""
计算两个方法名向量相似度，判断是否匹配
筛选出的api完全匹配（同名api）可能有多个，这种情况下继续根据类名来筛选，eg：

target api：Animator#start
source api：AnimatorSet#start，Animator#start
预期结果：Animator#start 和 Animator#start

返回格式：找到匹配：targetId,sourceMappingId;没有找到匹配：空字符串""
"""


def calculateStringSimilarity(sourceApi, targetApis, apiVectorDict: dict, classNameVectorDict: dict):
    global stringSimThreshold
    targetApiSimDict = dict()
    sourceApiVec = apiVectorDict[str(sourceApi[0])]

    for targetApi in targetApis:
        targetApiVec = apiVectorDict[str(targetApi[0])]
        # dict结构：key：apiId，value：相似度
        targetApiSimDict[targetApi[0]] = cos_sim(sourceApiVec, targetApiVec)

    # 改为二级筛选，先按照api来筛选，如果有多个最高相似匹配，就再按照class name来筛选
    # targetApiMappingTopKList：[1:1, 2:1, 3:1]
    targetApiMappingTopKList = sorted(targetApiSimDict.items(), key=lambda x: x[1], reverse=True)[0:5]
    # 字符相似度小于0.9995, 直接返回没匹配上 targetApiMappingTopKList[0][1]:第1个匹配对的相似度大小
    if targetApiMappingTopKList is None or len(targetApiMappingTopKList) == 0 \
        or targetApiMappingTopKList[0] is None or len(targetApiMappingTopKList[0]) < 1:
        return ""
    topApiSimilarity = targetApiMappingTopKList[0][1]
    if topApiSimilarity < stringSimThreshold:
        return ""

    topSimApiDictList = list()
    for apiDict in targetApiMappingTopKList:
        if apiDict[1] >= topApiSimilarity:
            topSimApiDictList.append(apiDict)

    # 二级筛选
    # 如果匹配出的安卓api只有1个，直接返回匹配结果
    if len(topSimApiDictList) == 1:
        return str(sourceApi[0]) + '-' + str(topSimApiDictList[0][0])

    # 二级筛选：根据class name的字符相似度选出最匹配的
    targetClassNameSimDict = dict()
    sourceClassNameVec = classNameVectorDict[str(sourceApi[0])]
    # targetApiDict：key：apiId value：classNameVector
    for targetSimApiDict in topSimApiDictList:
        targetClassNameSimDict[targetSimApiDict[0]] = cos_sim(sourceClassNameVec,
                                                              classNameVectorDict[str(targetSimApiDict[0])])
    targetClassNameSimDictTopKList = sorted(targetClassNameSimDict.items(), key=lambda x: x[1], reverse=True)[0:2]

    top = targetClassNameSimDictTopKList[0]

    return str(sourceApi[0]) + '-' + str(top[0])


def calculateMethodDocumentSimilarity(sourceApi, targetApis, classCommentVectorDict, methodCommentVectorDict,
                                      methodParamReturnVectorDict):
    global methodDocumentSimThreshold
    targetSimDict = dict()
    sourceClassCommentVec = classCommentVectorDict[str(sourceApi[0])]
    sourceMethodCommentVec = methodCommentVectorDict[str(sourceApi[0])]
    sourceMethodParamReturnVec = methodParamReturnVectorDict[str(sourceApi[0])]

    for targetApi in targetApis:
        targetClassCommentVec = classCommentVectorDict[str(targetApi[0])]
        targetMethodCommentVec = methodCommentVectorDict[str(targetApi[0])]
        targetMethodParamReturnVec = methodParamReturnVectorDict[str(targetApi[0])]

        classCommentSim = cos_sim(sourceClassCommentVec, targetClassCommentVec)
        methodCommentSim = cos_sim(sourceMethodCommentVec, targetMethodCommentVec)
        methodParamReturnCommentSim = cos_sim(sourceMethodParamReturnVec, targetMethodParamReturnVec)

        # 加权平均
        methodDocumentSim = 0.2 * classCommentSim + 0.8 * methodCommentSim + 0.1 * methodParamReturnCommentSim
        targetSimDict[targetApi[0]] = methodDocumentSim

    # 取最高相似度
    targetMappingTopKList = sorted(targetSimDict.items(), key=lambda x: x[1], reverse=True)[0:5]
    if targetMappingTopKList is None or len(targetMappingTopKList) == 0 \
        or targetMappingTopKList[0] is None or len(targetMappingTopKList[0]) < 1:
        return ""

    topSimilarity = targetMappingTopKList[0][1]

    if topSimilarity >= methodDocumentSimThreshold:
        return str(sourceApi[0]) + '-' + str(targetMappingTopKList[0][0])
    else:
        return ""


def calculateApiMappings(sourceApis, targetApis,
                         apiVectorDict,
                         classVectorDict,
                         classCommentVectorDict,
                         methodCommentVectorDict,
                         methodParamReturnVectorDict
                         ):
    """
    计算api mapping

    """
    # [1-2,3-4,5-6]
    apiMappings = list()
    for sourceApi in sourceApis:

        # 方法名相似度匹配
        result = calculateStringSimilarity(sourceApi, targetApis, apiVectorDict, classVectorDict)
        if result != "":
            apiMappings.append(result)
            continue

        # 方法名关联词相似度匹配
        result = calculateTokenRelatedSimilarity(sourceApi, targetApis)
        if result != "":
            apiMappings.append(result)
            continue

        # 方法文档注释相似度匹配
        result = calculateMethodDocumentSimilarity(sourceApi, targetApis, classCommentVectorDict,
                                                   methodCommentVectorDict,
                                                   methodParamReturnVectorDict)
        if result != "":
            apiMappings.append(result)

    return apiMappings


def generateWordSysnDict(taskId: str, dbFilepath: str):
    """
    生成word -> wn.synsets 字典，优化性能
    :param taskId:
    :param dbFilepath:
    :return:
    """
    global wordSysnDict
    if len(wordSysnDict.keys()) != 0:
        return

    conn = sqlite3.connect(dbFilepath)
    cursor = conn.cursor()
    # api_basic表结构:
    # id, task_id, filepath, class_name, api_name,
    # type, method_word_sequence, token_sequence, token_vector
    apiQuerySql = "select method_word_sequence from api_basic where task_id = " + taskId
    cursor.execute(apiQuerySql)
    methodWordSequences = cursor.fetchall()

    for wordSeq in methodWordSequences:
        words = wordSeq[0].split(',')
        for word in words:
            # 已经处理过该word则直接跳过，避免重复处理
            if wordSysnDict.keys().__contains__(word):
                continue
            wordSysnDict[word] = wn.synsets(word)


def generatePhraseSimGraph(sourceWords, targetWords):
    """
    phrase1: set pro1
    phrase2: set pro2

    =>

    graph:
    [
     [1,0],
     [0,0]
    ]
    """
    global wordSysnDict
    global wordSimDict

    graph = list()
    for sourceWord in sourceWords:
        wordSimList = list()
        for targetWord in targetWords:
            path_sim = 0
            wordSimKey = str(sourceWord) + "_" + str(targetWord)
            # 性能优化：之前已经计算过两个word之间的相似度了，那就返回之前的计算结果
            if wordSimDict.keys().__contains__(wordSimKey):
                path_sim = wordSimDict[wordSimKey]
            else:
                sourceSynset = wordSysnDict[sourceWord]
                targetSynset = wordSysnDict[targetWord]
                for ss in sourceSynset:
                    for ts in targetSynset:
                        sim = ss.path_similarity(ts)
                        if sim:
                            path_sim = max(path_sim, sim)
                wordSimDict[wordSimKey] = path_sim
            wordSimList.append(path_sim)
        graph.append(wordSimList)
    return graph


def calculatePhraseTotalSim(graph):
    """
    使用km算法来计算graph（两个短语组成）最大权值
    这里权值就是用wordnet计算出的两个单词的相似度
    :param graph:
    :return:
    """
    # km算法默认计算的是最小权匹配，这里做处理改为最大权值匹配
    cost_graph = []
    for row in graph:
        cost_row = []
        for col in row:
            cost_row += [- col]
        cost_graph += [cost_row]

    m = Munkres()
    indexes = m.compute(cost_graph)

    total = 0
    for row, column in indexes:
        value = graph[row][column]
        total += value

    return total


def calculatePhraseSimilarity(sourcePhrase, targetPhrase):
    """
    计算两个phrase之间的相似度

    :param sourcePhrase: set pro1
    :param targetPhrase: set pro2
    :return:
    """
    global phraseSimDict
    phraseSimKey = str(sourcePhrase) + "_" + str(targetPhrase)
    # 之前已经计算过两个phrase之间的相似度，直接返回
    if phraseSimDict.keys().__contains__(phraseSimKey):
        return phraseSimDict[phraseSimKey]
    else:
        sourceWords = sourcePhrase.split(',')
        targetWords = targetPhrase.split(',')
        graph = generatePhraseSimGraph(sourceWords, targetWords)
        totalSim = calculatePhraseTotalSim(graph)
        sim = totalSim / ((len(sourceWords) + len(targetWords)) / 2)
        phraseSimDict[phraseSimKey] = sim

        return sim


"""
计算两个方法名关联词相似度，判断是否匹配
"""


def calculateTokenRelatedSimilarity(sourceApi, targetApis):
    global tokenRelatedSimThreshold
    for targetApi in targetApis:
        sim = calculatePhraseSimilarity(sourceApi[6], targetApi[6])
        if sim > tokenRelatedSimThreshold:
            return str(sourceApi[0]) + '-' + str(targetApi[0])
    return ""


if __name__ == "__main__":
    taskId = sys.argv[1]
    dbFilepath = sys.argv[2]
    # 生成关联词词典
    generateWordSysnDict(taskId, dbFilepath)
    # 加载api数据
    sourceApis, targetApis = loadApiDataFromSQLLite(taskId, dbFilepath)

    apiVectorDictFilepath = sys.argv[3]
    classNameVectorDictFilepath = sys.argv[4]
    classCommentVectorDictFilepath = sys.argv[5]
    methodCommentVectorDictFilepath = sys.argv[6]
    methodParamReturnVectorDictFilepath = sys.argv[7]

    apiVectorDict = getVectorDict(apiVectorDictFilepath)
    classVectorDict = getVectorDict(classNameVectorDictFilepath)
    classCommentVectorDict = getVectorDict(classCommentVectorDictFilepath)
    methodCommentVectorDict = getVectorDict(methodCommentVectorDictFilepath)
    methodParamReturnVectorDict = getVectorDict(methodParamReturnVectorDictFilepath)

    # 计算mapping
    resultLine = calculateApiMappings(sourceApis, targetApis,
                                      apiVectorDict,
                                      classVectorDict,
                                      classCommentVectorDict,
                                      methodCommentVectorDict,
                                      methodParamReturnVectorDict
                                      )

    print(resultLine)


# if __name__ == '__main__':
#     taskId = '1'
#     dbFilepath = '/Users/gaoyi/IdeaProjects/TestMigrationV2/data.db'
#     sourceApis, targetApis = loadApiDataFromSQLLite(taskId, dbFilepath)
#
#     classCommentVectorDictFilepath = '/Users/gaoyi/IdeaProjects/TestMigrationV2/model/word2vec/classCommentVectorDict.txt'
#     methodCommentVectorDictFilepath = '/Users/gaoyi/IdeaProjects/TestMigrationV2/model/word2vec/methodCommentDict.txt'
#     methodParamReturnVectorDictFilepath = '/Users/gaoyi/IdeaProjects/TestMigrationV2/model/word2vec/methodParamReturnVectorDict.txt'
#
#     classCommentVectorDict = getVectorDict(classCommentVectorDictFilepath)
#     methodCommentVectorDict = getVectorDict(methodCommentVectorDictFilepath)
#     methodParamReturnVectorDict = getVectorDict(methodParamReturnVectorDictFilepath)
#
#     for sourceApi in sourceApis:
#         sim = calculateMethodDocumentSimilarity(sourceApi, targetApis, classCommentVectorDict, methodCommentVectorDict,
#                                       methodParamReturnVectorDict)
#         # print(sim)
