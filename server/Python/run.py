#!/usr/bin/env python
# -*- coding: utf-8 -*-

import BaseHTTPServer
import json
import subprocess
import time
from collections import OrderedDict

PORT = 8079  # 闪讯机器人API服务器监听端口
INTERFACE = 'wan'  # OpenWrt下外网接口的名称
SECRET = '123456'  # API服务器通信密钥


def get_username():
    result = subprocess.check_output(['uci', 'get', 'network.{}.username'.format(INTERFACE)])
    return result


def get_password():
    result = subprocess.check_output(['uci', 'get', 'network.{}.password'.format(INTERFACE)])
    return result


def set_username(username):
    subprocess.call(['uci', 'set', 'network.{}.username={}'.format(INTERFACE, username)])
    return get_username()


def set_password(password):
    subprocess.call(['uci', 'set', 'network.{}.password={}'.format(INTERFACE, password)])
    return get_password()


class SingleNetRequestHandler(BaseHTTPServer.BaseHTTPRequestHandler):

    def handle_method(self, method):
        route = self.path.strip('/')
        if route == '':
            now = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
            self.send_content(now)
        elif route == 'wan_option':
            if method == 'GET':
                self.get_wan_option()
            elif method == 'POST':
                self.set_wan_option()
            else:
                self.send_error(405)
        else:
            self.send_error(404)

    def get_wan_option(self):
        data = OrderedDict([('username', get_username()), ('password', get_password())])
        self.send_content(data)

    def set_wan_option(self):
        data = self.get_payload()
        username = data['username']
        password = data['password']
        if username == '' and password == '':
            self.send_error(400)
            return
        if username:
            set_username(username)
        if password:
            set_password(password)
        self.get_wan_option()

    def get_payload(self):
        payload_len = int(self.headers.getheader('Content-Length', 0))
        payload = self.rfile.read(payload_len)
        payload = json.loads(payload)
        return payload

    def send_content(self, data):
        self.send_response(200)
        self.send_header('Content-Type', 'application/json;charset=UTF-8')
        self.end_headers()
        self.wfile.write(self.api_response(data))

    @staticmethod
    def api_response(data):
        response = OrderedDict([('code', 2000), ('message', 'success'), ('data', data)])
        return json.dumps(response).replace('\\n', '')

    def do_GET(self):
        self.handle_method('GET')

    def do_POST(self):
        self.handle_method('POST')

    def do_PUT(self):
        self.handle_method('PUT')

    def do_DELETE(self):
        self.handle_method('DELETE')


if __name__ == '__main__':
    http_server = BaseHTTPServer.HTTPServer(('', PORT), SingleNetRequestHandler)
    print('Starting SingleNet Robot API Server at port %d' % PORT)
    try:
        http_server.serve_forever()
    except KeyboardInterrupt:
        pass
    print("Stopping SingleNet Robot API Server")
    http_server.server_close()