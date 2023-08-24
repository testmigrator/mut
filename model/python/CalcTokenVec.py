import json
import sqlite3

import numpy as np
from gensim.models import word2vec, Word2Vec
import sys


def loadApiDataFromSQLLite(taskId: str, dbFilepath: str):
    conn = sqlite3.connect(dbFilepath)
    cursor = conn.cursor()
    # id, task_id, filepath, class_name, api_name,
    # type, method_word_sequence, token_sequence, token_vector, class_name_token_sequence
    apiQuerySql = "select * from api_basic where task_id = " + taskId
    cursor.execute(apiQuerySql)
    return cursor.fetchall()




def generateVectorDict(apis, modelFilepath: str, vectorDictFilepath: str, type: str):
    np.set_printoptions(linewidth=np.inf)
    model = Word2Vec.load(modelFilepath)
    word_vectors = model.wv
    wordVecDict = dict()
    for key in word_vectors.index_to_key:
        wordVecDict[key] = word_vectors[key]

    apiVectorDict = dict()
    for api in apis:
        tokenKeys = list()
        if type == 'api':
            tokenKeys = api[7].split(',')

        if type == 'className':
            tokenKeys = api[9].split(',')

        if type == 'classComment':
            tokenKeys = api[10].split(',')

        if type == 'methodComment':
            tokenKeys = api[11].split(',')

        if type == 'methodParamReturn':
            tokenKeys = api[12].split(',')

        if len(tokenKeys) == 0 or tokenKeys[0] not in wordVecDict.keys():
            apiVectorDict[api[0]] = '0'
        else:
            tmp = wordVecDict[tokenKeys[0]]
            for index in range(1, len(tokenKeys)):
                tmp = tmp + wordVecDict[tokenKeys[index]]
            tmp = tmp / len(tokenKeys)
            apiVectorDict[api[0]] = tmp

    with open(vectorDictFilepath, 'w') as dictFile:
        for k, v in apiVectorDict.items():
            array_str = ','.join(str(i) for i in v)
            dictFile.writelines(str(k) + "|" + array_str + "\n")


if __name__ == '__main__':
    # //ApiVectorDictFilepath
    apis = loadApiDataFromSQLLite(sys.argv[1], sys.argv[2])
    modelFilepath = sys.argv[3]
    apiVectorDictFilepath = sys.argv[4]
    classNameVectorDictFilepath = sys.argv[5]
    classCommentVectorDictFilepath = sys.argv[6]
    methodCommentVectorDictFilepath = sys.argv[7]
    methodParamReturnVectorDictFilepath = sys.argv[8]

    generateVectorDict(apis, modelFilepath, apiVectorDictFilepath, "api")
    generateVectorDict(apis, modelFilepath, classNameVectorDictFilepath, "className")
    generateVectorDict(apis, modelFilepath, classCommentVectorDictFilepath, "classComment")
    generateVectorDict(apis, modelFilepath, methodCommentVectorDictFilepath, "methodComment")
    generateVectorDict(apis, modelFilepath, methodParamReturnVectorDictFilepath, "methodParamReturn")
