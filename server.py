from http.server import HTTPServer, BaseHTTPRequestHandler #класс
import qrcode
import random
import socket
from io import BytesIO #класс

toclientmessage=''
def get_local_ip():
    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)
    return local_ip

def random_token():
    str1 = '123456789'
    str2 = 'qwertyuiopasdfghjklzxcvbnm'
    str3 = str2.upper()
    str4 = str1+str2+str3
    ls = list(str4)
    random.shuffle(ls)
    psw = ''.join([random.choice(ls) for x in range(12)])
    return psw

class SimpleHTTPRequestHandler(BaseHTTPRequestHandler):
    
    
    # определяем метод `do_GET` 
    def do_GET(self):
        global toclientmessage
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        
        print(toclientmessage)
        if(toclientmessage==''):
            self.wfile.write(str.encode("empty"))
        else:
            self.wfile.write(str.encode("w"))
            toclientmessage=''
        

    # определяем метод `do_POST` 
    def do_POST(self):
        global toclientmessage
        content_length = int(self.headers['Content-Length'])
        body = self.rfile.read(content_length)
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        response = BytesIO()
        response.write(b'This is POST request. ')
        response.write(b'Received: ')
        response.write(body)
        self.wfile.write(response.getvalue())

        toclientmessage="notnull"
        print(body)

    
  
data = "https://" + str(get_local_ip()) + ":8080-"+str(random_token())
filename = "site.png"
img = qrcode.make(data)
img.save(filename)
print(get_local_ip())
httpd = HTTPServer(('', 8080), SimpleHTTPRequestHandler)
httpd.serve_forever()
