from enum import IntEnum
from collections import deque


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


def getc():
    global txt
    if txt != '':
        symbol = txt[0]
        txt = txt[1:]
        return symbol
    else:
        return '\0'


def superLetter(ch):
    if ch.isalpha() or ch == '_':
        return True
    return False


def superLetterOrDigit(ch):
    if ch.isalnum() or ch == '_':
        return True
    return False


def scanner():
    global txt
    token_list = deque([])
    while txt:
        token = tokenType()
        id = ''
        # get next symbol until no space characters apper
        ch = getc()
        while ch.isspace():
            ch = getc()
        # identifier or keyword
        if superLetter(ch):
            while True:  # superLetterOrDigit(ch):
                id += ch
                if not superLetterOrDigit(txt[0]):
                    break
                ch = getc()
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
    return token_list


def nextSymbol():
    global token
    if not token:
        raise sementicError()
    return token[0]


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
        return True
    elif symbol.number == TSymbol.tcomma:
        L()
    else:
        raise sementicError()


def parser():
    while token:
        D()


def tip():
    print('press enter if you want to exit.')


if __name__ == '__main__':
    tip()
    print('start Compiler')
    while True:
        txt = input("sentence : ")
        if not txt:
            break
        try:
            # print('start scanner')
            token = scanner()
            # print('start parser')
            parser()
        except Exception as e:
            print("Error -", e)
        else:
            print("Success!")
