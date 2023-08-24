import CppHeaderParser

# cppHeader = CppHeaderParser.CppHeader("../doc/harmony/demo/SimpleClass.h")


# print(cppHeader.includes)

# for classname in cppHeader.classes.keys():
#     print(classname)

# print(cppHeader.classes['SampleClass']['namespace'])
# print(cppHeader.classes['AlphaClass']['namespace'])
# print(cppHeader.classes['OmegaClass']['namespace'])

# for oneMethod in cppHeader.classes['AlphaClass']['methods']['public']:
#     print(oneMethod['name'])
# for kv in oneMethod.items():
#     print(kv[0], " : ", kv[1])

# print('---------------------\n')

import json
# from cppExtractor.HarmonyApiDocument import HarmonyApiDocument


import sys
import os

delimiter = '$$$'

def fetchCppHFileApiBasic(filepath: str):
    # classComment $$$ className $$$ methodComment $$$ methodName $$$ methodParameter $$$ methodReturn
    apiResultLines = list()
    failParseFiles = list()

    lines = list()
    with open(filepath, 'r') as file:
        for line in file.readlines():
            lines.append(line)

    newfilePath = filepath + '.tmp'

    with open(newfilePath, 'w') as file:
        for line in lines:
            if line.__contains__("std::ios::sync_with_stdio"):
                continue
            line = line.replace('//', '///')
            file.write(line.rstrip() + '\n')

    try:
        cppHeader = CppHeaderParser.CppHeader(newfilePath)
        for className in cppHeader.classes.keys():
            cls = cppHeader.classes[className]
            classComment = ''
            if 'doxygen' in cls.keys():
                classComment = cls['doxygen']

            for method in cls['methods']['public']:
                methodComment = ''
                methodName = method['name']
                if 'doxygen' in method.keys():
                    methodComment = method['doxygen']

                methodParameter = ''
                if 'parameters' in method.keys():
                    # type name
                    parameters = method['parameters']
                    for param in parameters:
                        methodParameter = methodParameter + ' ' + param['type'] + ' ' + param['name']

                methodReturn = ''
                if 'returns' in method.keys():
                    methodReturn = method['returns']

                # classComment $$$ className $$$ methodComment $$$ methodName $$$ methodParameter $$$ methodReturn
                apiResultLine = classComment + delimiter + className + delimiter + methodComment + delimiter \
                                + methodName + delimiter + methodParameter + delimiter + methodReturn

                apiResultLine = apiResultLine.replace('*', '') \
                    .replace('/', '') \
                    .replace('!', '') \
                    .replace('//', '') \
                    .replace('\\', '') \
                    .replace('\n', '') \
                    .replace('\t', '') \
                    .replace('@param', '') \
                    .replace('@Param', '') \
                    .replace('@type', '') \
                    .replace('@Type', '') \
                    .replace('/param', '') \
                    .replace('/Param', '')
                apiResultLines.append(apiResultLine)

        namespaces = cppHeader.namespaces
        if namespaces is not None:
            namespace = namespaces[len(namespaces) - 1]
            functions = cppHeader.functions
            # classComment $$$ className $$$ methodComment $$$ methodName $$$ methodParameter $$$ methodReturn
            for function in functions:
                apiResultLine = "" + delimiter + namespace + delimiter + "" + delimiter \
                            + function['name'] + delimiter + "" + delimiter + ""
                apiResultLines.append(apiResultLine)

    except Exception as e:
        failParseFiles.append(filepath)

    if os.path.exists(newfilePath):
        os.remove(newfilePath)

    print(list(set(apiResultLines)))

    return apiResultLines

if __name__ == '__main__':
    fetchCppHFileApiBasic(sys.argv[1])

    # filepath = '../doc/harmony/demo/SimpleClass2.h'
    # filepath = '../doc/harmony/demo/anticipate_curve.h'
    # headerFileName = '../doc/harmony/demo/animator.h'
    # filepath = '../doc/harmony/demo/SimpleClass.h'
    # fetchCppHFileApiBasic(headerFileName)
