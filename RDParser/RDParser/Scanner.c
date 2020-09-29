#include "Scanner.h"

enum tsymbol tnum[NO_KEYWORD] = {
	tlabel, tinteger
};

const char* tokenName[] = {
	"label", "integer", "%ident", ",", ";", "eof"
};

const char* keyword[NO_KEYWORD] = {
	"label", "integer"
};

struct tokenType scanner(FILE* sourceFile)
{
	struct tokenType token;
	int i, index;
	char ch, id[ID_LENGTH];
	token.number = tnull;

	do {
		while (isspace(ch = fgetc(sourceFile)));
		if (superLetter(ch)) { // identifier or keyword
			i = 0;
			do {
				if (i < ID_LENGTH)
					id[i++] = ch;
				ch = fgetc(sourceFile);
			} while (superLetterOrDigit(ch));
			if (i >= ID_LENGTH)
				lexicalError(1);
			id[i] = '\0';
			ungetc(ch, sourceFile);		// retract
			//find the identifier in the keyword table
			for (index = 0; index < NO_KEYWORD; index++)
				if (!strcmp(id, keyword[index]))
					break;
			if (index < NO_KEYWORD)		// found, keyword exit
				token.number = tnum[index];
			else
			{
				token.number = tident;
				strcpy_s(token.value.id, ID_LENGTH, id);
			}
		}	// end of identifier or keyword
		else switch (ch) {
		case ',':
			token.number = tcomma;
			break;
		case ';':
			token.number = tsemicolon;
			break;
		case EOF:
			token.number = teof;
			break;
		default: {
			printf("Current character : %c", ch);
			lexicalError(2);	// invalid character
			break;
		}
		}	// switch end
	} while (token.number == tnull);
	return token;
}	// end of scanner

int superLetter(char ch)
{
	if (isalpha(ch) || ch == '_') return 1;
	else return 0;
}

int superLetterOrDigit(char ch)
{
	if (isalnum(ch) || ch == '_') return 1;
	else return 0;
}

void writeToken(struct tokenType token, FILE* outputFile)
{
	if (token.number == tident)
	{
		fprintf(outputFile, "Token %10s ( %3d, %12s )\n", tokenName[token.number], token.number, token.value.id);
	}
	else
	{
		fprintf(outputFile, "Token %10s ( %3d, %12s )\n", tokenName[token.number], token.number, "0");
	}
}

void lexicalError(int n)
{
	printf(" *** Lexical Error : ");
	switch (n) {
	case 1: printf("an identifier length must be less than 12.\n");
		break;
	case 2: printf("invalid character\n");
		break;
	case 3: printf("string length must be less than 12.\n");
		break;
	case 4: printf("You need to close document\n");
		break;
	default: printf("Error.\n");
		break;
	}
}