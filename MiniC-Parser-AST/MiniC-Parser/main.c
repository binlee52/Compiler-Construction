#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <stdlib.h>

FILE* astFile;                          // AST file
FILE* sourceFile;                       // miniC source program
FILE* ucodeFile;                        // ucode file

#include "scanner.c"
#include "parser.c"
#include "sdt.c"

void icg_error(int n)
{
	printf("icg_error: %d\n", n);
	//3:printf("A Mini C Source file must be specified.!!!\n");
	//"error in DCL_SPEC"
	//"error in DCL_ITEM"
}

int main(int argc, char* argv[])
{
	char fileName[30] = "", buffer[30] = "";
	int errorNo = 0;
	char* fileNamePtr = NULL, * context = NULL;
	Node *root;

	printf(" *** start of Mini C Compiler\n");
	if (argc != 2) {
		icg_error(1);
		exit(1);
	}
	strncpy_s(fileName, sizeof(fileName), argv[1], sizeof(fileName) - 1);
	printf("   * source file name: %30s\n", fileName);

	if ((sourceFile = fopen(fileName, "r")) == NULL) {
		icg_error(2);
		exit(1);
	}
	astFile = fopen(strcat(strtok(fileName, "."), ".ast"), "w");

	printf(" === start of Parser\n");
    root = parser();
	printTree(root, 0);
	printf(" *** end   of Mini C Compiler\n");
}