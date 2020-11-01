from enum import IntEnum
from collections import deque
from time import time

class tokenType:
    def __init__(self, number=-1, value=''):
        self.number = number
        self.value = value


class TSymbol(IntEnum):
    tnull = -1
    tlabel = 0
    tinteger = 1
    tident = 2
    tsemicolon = 3
    tcomma = 4


class Keyword(IntEnum):
    tlabel = 0
    tinteger = 1


class lexicalError(Exception):
    def __init__(self, n=0):
        self.n = n
        e = 'invalid character'
        super().__init__(e)


class sementicError(Exception):
    def __init__(self, n=0):
        self.n = n
        e = 'sementic error'
        super().__init__(e)


TOKENNAME = ['label', 'integer', '%ident', ',', ';']
KEYWORD = ['label', 'integer']
tnum = [TSymbol.tlabel, TSymbol.tinteger]
cursor = 0


# get char from txt
def getc():
    global txt, cursor
    if cursor != len(txt):
        cursor += 1
        return txt[cursor-1]
    return '\0'


# move cursor front
def ungetc(ch):
    global cursor
    cursor -= 1


# check char is alphabet or _
def superLetter(ch):
    if ch.isalpha() or ch == '_':
        return True
    return False


# check char is alphabet, _, digit
def superLetterOrDigit(ch):
    if ch.isalnum() or ch == '_':
        return True
    return False


class lexicalError(Exception):
    def __init__(self, n=0):
        self.n = n
        e = 'invalid character'
        super().__init__(e)


class sementicError(Exception):
    def __init__(self, n=0):
        self.n = n
        e = 'sementic error'
        super().__init__(e)


# Scanner in Compiler
def scanner():
    token_list = deque([])
    while True:
        token = tokenType()
        id = ''
        # get next symbol until no space characters apper
        ch = getc()
        while ch.isspace():
            ch = getc()
        # identifier or keyword
        if superLetter(ch):
            while superLetterOrDigit(ch):
                id += ch
                ch = getc()
            ungetc(ch)
            # found, keyword exit
            if id in KEYWORD:
                token.number = tnum[KEYWORD.index(id)]
            else:
                token.number = TSymbol.tident
                token.value = id
            # end of identifier or keyword
        elif ch == ',':
            token.number = TSymbol.tcomma
        elif ch == ';':
            token.number = TSymbol.tsemicolon
        elif ch == '\0':
            return token_list
            # break
        else:
            raise lexicalError()
        token_list.append(token)


# show next token
def nextSymbol():
    global token
    if not token:
        raise sementicError()
    return token[0]


# return token
def get_nextSymbol():
    global token
    if not token:
        raise sementicError()
    return token.popleft()


def D():
    symbol = get_nextSymbol()
    if symbol.number == TSymbol.tlabel:
        L()
    elif symbol.number == TSymbol.tinteger:
        L()
    else:
        raise sementicError()


def L():
    symbol = get_nextSymbol()
    if symbol.number == TSymbol.tident:
        R()
    else:
        raise sementicError()


def R():
    symbol = get_nextSymbol()
    if symbol.number == TSymbol.tsemicolon:
        return
    elif symbol.number == TSymbol.tcomma:
        L()
    else:
        raise sementicError()


def EBNF():
    symbol = get_nextSymbol()
    # 현재 token이 label 혹은 integer 일 때
    if (symbol.number == TSymbol.tlabel) or (symbol.number == TSymbol.tinteger):
        symbol = get_nextSymbol()   # token 하나를 가져온다
        if symbol.number == TSymbol.tident:     # 현재 token이 ident 일 때
            symbol = get_nextSymbol()   # token 하나를 가져온다
            # 현재 token이 ','일 때
            while symbol.number == TSymbol.tcomma:
                symbol = get_nextSymbol()       # token 하나를 가져온다
                # token이 ident 일 때 다음 token을 가져온다
                if symbol.number == TSymbol.tident:
                    symbol = get_nextSymbol()
                # token이 ident가 아니라면 error
                else:
                    raise sementicError()
            # 더이상 ','가 나오지 않고, 현재 token이 ';' 일 때 문법에 맞는 문장
            if symbol.number == TSymbol.tsemicolon:
                return
            # ';'가 나오지 않으면 문법에 맞지 않는 문장
            else:
                raise sementicError()
        else:
            raise sementicError()
    else:
        raise sementicError()


def parser_BNF():
    while token:
        D()


def parser_EBNF():
    while token:
        EBNF()


def tip(n = 0):
    if n == 0:
        print("------------BNF-PARSER-----------")
        print("<D> ::= 'label'<L> | 'integer'<L>")
        print("<L> ::= <id><R>")
        print("<R> ::= ';' | ','<L>")
        print('press enter if you want to exit.\n')
    elif n == 1:
        print("------------EBNF-PARSER-----------")
        print("('label'|'integer')<id>{','<id>}';'")
        print('press enter if you want to exit.\n')



if __name__ == '__main__':
    txt = 'integer x, y, z; label a;' * 500000 +'\0'
    print("BNF-Parser(0), EBNF-Parser(1)")
    n = int(input('Choose Parser : '))
    if n not in [0, 1]:
        n = int(input('Choose Parser(0, 1) : '))
    tip(n)
    while True:
        #txt = input("sentence : ") + '\0'
        cursor = 0
        if txt == '\0':
            break
        try:
            print('start scanner', end="")
            token = scanner()
            print(' - end')
            print('start parser', end="")
            start = time()
            if n == 0:
                parser_BNF()
            elif n == 1:
                parser_EBNF()
            print(' - end')
        except Exception as e:
            print("\nError -", e)
        else:
            print("Success!")
            print("parsing 시간: {:.4} s".format(time() - start))
        print('')
        break