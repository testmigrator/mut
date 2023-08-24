
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
    for m1token in m1tokens:
        maxSimScore = 0
        tmpM2Token = ""
        for m2token in m2tokens:
            score = calcMaxSimScore(m1token, m2token)
            if score > maxSimScore:
                maxSimScore = score
                tmpM2Token = m2token
        if maxSimScore > 0:
            alreadyMatchM2Tokens.append(tmpM2Token)
            matchTokenLen = matchTokenLen + 2

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
