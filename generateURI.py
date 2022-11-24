import sys

from faker import Faker

if __name__ == "__main__":
    num = int(sys.argv[1])

    # 默认生成的数据为为英文，使用zh_CN指定为中文
    # f = Faker('zh_CN')

    fake = Faker()

    for i in range(num):
        print(fake.uri())
