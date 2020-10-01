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

def getc():
    global txt, cursor
    if cursor != len(txt):
        cursor += 1
        return txt[cursor-1]
    else:
        return '\0'


def ungetc(ch):
    global cursor
    cursor -= 1


def superLetter(ch):
    if ch.isalpha() or ch == '_':
        return True
    return False


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


# txt = 'integer x, y, z;'
def scanner():
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
    #txt = 'integer x, y, z; label a;' * 100000
    while True:
        txt = input("sentence : ")
        cursor = 0
        if not txt:
            break
        try:
            print('start scanner', end="")
            token = scanner()
            print(' - end')
            print('start parser', end="")
            start = time()
            parser()
            print(' - end')
        except Exception as e:
            print("Error -", e)
            print("parsing 시간: {:.4} s".format(time() - start))
        else:
            print("Success!")
            print("parsing 시간: {:.4} s".format(time() - start))
        print('')
