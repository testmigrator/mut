methodDeclaration
	:	methodModifier* methodHeader methodBody
	;

methodBody
	:	block
	|	';'
	;

block
	:	'{' blockStatements? '}'
	;

blockStatements
	:	blockStatement+
	;

blockStatement
	:	localVariableDeclarationStatement
	|	classDeclaration
	|	statement
	;