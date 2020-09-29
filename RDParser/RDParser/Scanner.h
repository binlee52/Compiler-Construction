#ifndef SCANNER_H

#define SCANNER_H 1

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <math.h>

#define NO_KEYWORD 2
#define ID_LENGTH 12

struct tokenType {
	int number;		// token number
	union {
		char id[ID_LENGTH];
	} value;
};

enum tsymbol {
	tnull = -1,
	tlabel, tinteger, tident, tsemicolon, tcomma, teof
	/*   0         1       2           3       4*/
} tsymbol;

int superLetter(char ch);

int superLetterOrDigit(char ch);

void writeToken(struct tokenType token, FILE* outputFile);

void lexicalError(int n);


#endif // !SCANNER_H
