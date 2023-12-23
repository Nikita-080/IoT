import qrcode


import socket
 
def get_local_ip():
    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)
    return local_ip
print(get_local_ip())

#http://[ip]:8080-[token]
# пример данных
data = "https://" + str(get_local_ip()) + ":8080-1111"
# имя конечного файла
filename = "site.png"
# генерируем qr-код
img = qrcode.make(data)
# сохраняем img в файл
img.save(filename)
