import json
import sqlite3

import numpy as np
from gensim.models import word2vec, Word2Vec
import sys


def loadApiDataFromSQLLite(taskId: str, dbFilepath: str):
    conn = sqlite3.connect(dbFilepath)
    cursor = conn.cursor()
    # api_basic表结构:
    # id, task_id, filepath, class_name, api_name,
    # type, method_word_sequence, token_sequence, token_vector, class_name_token_sequence
    apiQuerySql = "select * from api_basic where task_id = " + taskId
    cursor.execute(apiQuerySql)
    return cursor.fetchall()


def generateVectorDict(apis, modelFilepath: str, vectorDictFilepath: str, type: str):
    np.set_printoptions(linewidth=np.inf)
    # 加载模型
    model = Word2Vec.load(modelFilepath)
    word_vectors = model.wv
    wordVecDict = dict()
    for key in word_vectors.index_to_key:
        wordVecDict[key] = word_vectors[key]

    apiVectorDict = dict()
    for api in apis:
        if type == 'api':
            tokenKeys = api[7].split(',')
        else:
            tokenKeys = api[9].split(',')

        # 可能遇到stop-word，跳过
        if len(tokenKeys) == 0 or tokenKeys[0] not in wordVecDict.keys():
            apiVectorDict[api[0]] = '0'
        else:
            # 向量的加和平均
            tmp = wordVecDict[tokenKeys[0]]
            for index in range(1, len(tokenKeys)):
                tmp = tmp + wordVecDict[tokenKeys[index]]
            tmp = tmp / len(tokenKeys)
            apiVectorDict[api[0]] = tmp

    with open(vectorDictFilepath, 'w') as dictFile:
        for k, v in apiVectorDict.items():
            array_str = ','.join(str(i) for i in v)
            dictFile.writelines(str(k) + "|" + array_str + "\n")


def generateApiVectorDict(apis, modelFilepath: str, apiVectorDictFilepath: str):
    generateVectorDict(apis, modelFilepath, apiVectorDictFilepath, "api")


def generateClassNameVectorDict(apis, modelFilepath: str, classNameVectorDictFilepath: str):
    generateVectorDict(apis, modelFilepath, classNameVectorDictFilepath, "className")


if __name__ == '__main__':
    # //ApiVectorDictFilepath
    apis = loadApiDataFromSQLLite(sys.argv[1], sys.argv[2])
    generateApiVectorDict(apis, sys.argv[3], sys.argv[4])
    generateClassNameVectorDict(apis, sys.argv[3], sys.argv[5])
