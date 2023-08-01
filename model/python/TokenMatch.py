#关联词匹配
# 关联词之间相似度阈值0.5（两个单词的相似度大于等于0.5认为相关联）

import sys
from nltk.corpus import wordnet as wn

methodName1 = "begin,animator"
methodName2 = "start,animator"


# 9 8
# value,was,set,on,start ,set,time,point
# sim score:  set set
# sim score:  set time
# sim score:  value set
# sim score:  was point
# sim score:  set set
# sim score:  set time
# sim score:  start set
# sim score:  start point

def calcMaxSimScore(token1: str, token2: str):
    max = 0
    token1Nets = wn.synsets(token1)
    # 找出每个维度含义的所有近义词，可能会很长
    for t in token1Nets:
        token2Nets = wn.synsets(token2)
        for tt in token2Nets:
            score = t.path_similarity(tt)
            if score is not None and score >= 0.5 and score > max:
                print("sim score: ", score)
                max = score

    return max

# calcMaxSimScore("value", "move")
#
# calcMaxSimScore("time", "set")

# 匹配算法
# methodName格式：equal,ignore,case
# todo 要重新设计！
def match(methodName1: str, methodName2: str):
    # print(methodName1)
    # print(methodName2)
    m1tokens = methodName1.split(",")
    m2tokens = methodName2.split(",")
    # print("m1tokens: ",m1tokens)
    # print("m2tokens: ",m2tokens)

    alreadyMatchM2Tokens = list()
    totalTokenLen = len(m1tokens) + len(m2tokens)
    matchTokenLen = 0
    # 这两个for是对方法名词序列遍历，不会太长
    for m1token in m1tokens:
        maxSimScore = 0
        tmpM2Token = ""
        for m2token in m2tokens:
            # 计算两个token 含义如果存在相近，相似度会不会大于0.5
            score = calcMaxSimScore(m1token, m2token)
            if score > maxSimScore:
                maxSimScore = score
                tmpM2Token = m2token
        if maxSimScore > 0:
            alreadyMatchM2Tokens.append(tmpM2Token)
            matchTokenLen = matchTokenLen + 2

    # 如果mis match 的token数目大于2 认为不匹配
    if totalTokenLen - matchTokenLen < 2:
        print(totalTokenLen, matchTokenLen)
        return True
    else:
        return False

if __name__ == '__main__':
#     match(sys.argv[1], sys.argv[2])
    #
    word1 = 'set,animation'
    word2 = 'set,animator'
    print(match(word1, word2))
    # print(calcMaxSimScore(word1, word2))
