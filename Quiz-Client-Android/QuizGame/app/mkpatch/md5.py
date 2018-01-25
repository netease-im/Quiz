import hashlib
import sys


def calcMD5(filepath):
    with open(filepath, 'rb') as f:
        md5obj = hashlib.md5()
        md5obj.update(f.read())
        hash = md5obj.hexdigest()
        print(hash)
        return hash


if __name__ == "__main__":
    if len(sys.argv) == 2:
        hashfile = sys.argv[1]
        calcMD5(hashfile)
    else:
        print("please input file path")
