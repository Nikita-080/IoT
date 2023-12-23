from http.server import HTTPServer, BaseHTTPRequestHandler #класс
import qrcode
import random
import socket

import io
import qrcode
from io import BytesIO #класс
import ssl

def random_token():
    str1 = '123456789'
    str2 = 'qwertyuiopasdfghjklzxcvbnm'
    str3 = str2.upper()
    str4 = str1+str2+str3
    ls = list(str4)
    random.shuffle(ls)
    psw = ''.join([random.choice(ls) for x in range(4)])
    return psw
toclientmessage=''
frommobile=''#Embedded
token=str(random_token())

def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("8.8.8.8", 80))
    return s.getsockname()[0]   
class SimpleHTTPRequestHandler(BaseHTTPRequestHandler):
    
    
    # определяем метод `do_GET` 
    def do_GET(self):
        print("receive get")
        global toclientmessage
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        User_Agent = str(self.headers['User-Agent'])
        User_Token = str(self.headers['token'])
        if(User_Agent=='mobile'):
            if(token==User_Token):  
                print(toclientmessage)
                if(toclientmessage==''):
                    self.wfile.write(str.encode("empty"))
                else:
                    self.wfile.write(str.encode("w"))
                    toclientmessage=''
            else:
                self.send_response(403)
                self.end_headers()
        else:
            self.wfile.write(str.encode(frommobile))
        

    # определяем метод `do_POST` 
    def do_POST(self):
        print("receive post")
        global toclientmessage
        content_length = int(self.headers['Content-Length'])
        User_Agent = str(self.headers['User-Agent'])
        User_Token = str(self.headers['token'])
        
        if(User_Agent=='mobile'):
            if(token==User_Token):
                frommobile = self.rfile.read(content_length)
                self.send_response(200)
                self.end_headers()
            else:
                self.send_response(403)
                self.end_headers()
        elif(User_Agent=='Embedded'):            
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
        else:
            self.send_response(400)
            self.end_headers()

    
  
data = "http://" + str(get_local_ip()) + ":8080-"+token
qr = qrcode.QRCode()
qr.add_data(data)
f = io.StringIO()
qr.print_ascii(out=f)
f.seek(0)
print(f.read())

print(get_local_ip())
print(token)
httpd = HTTPServer(('', 8080), SimpleHTTPRequestHandler)
ssl_context = ssl.SSLContext(protocol=ssl.PROTOCOL_TLS_SERVER)
ssl_context.load_cert_chain('./app.crt',keyfile='./app.key')
httpd.socket = ssl_context.wrap_socket (httpd.socket, server_side=True)
httpd.serve_forever()
