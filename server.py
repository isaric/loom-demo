import http.server
import socketserver
import subprocess
import urllib.request
import urllib.parse
import concurrent.futures
import time
import json

PORT = 8081

class MyHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/':
            self.path = '/index.html'
            return http.server.SimpleHTTPRequestHandler.do_GET(self)
        elif self.path.startswith('/api/demo1/'):
            # proxy to 8080
            target_path = self.path.replace('/api/demo1', '')
            try:
                req = urllib.request.urlopen('http://localhost:8080' + target_path)
                self.send_response(req.getcode())
                content_type = req.headers.get('Content-Type')
                if content_type:
                    self.send_header('Content-Type', content_type)
                self.end_headers()
                self.wfile.write(req.read())
            except Exception as e:
                self.send_response(500)
                self.end_headers()
                self.wfile.write(str(e).encode())
        elif self.path.startswith('/benchmark?'):
            query = urllib.parse.parse_qs(urllib.parse.urlparse(self.path).query)
            demo_type = query.get('type', ['virtual'])[0]
            concurrency = 40
            total_requests = 100
            url = f'http://localhost:8080/{demo_type}/report?id=42'
            
            latencies = []
            start_time = time.time()
            
            def make_req(i):
                req_start = time.time()
                try:
                    with urllib.request.urlopen(url) as r:
                        r.read()
                except Exception:
                    pass
                return time.time() - req_start

            with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as executor:
                latencies = list(executor.map(make_req, range(total_requests)))
                
            total_time = time.time() - start_time
            latencies.sort()
            
            stats = {
                "type": demo_type,
                "total_requests": total_requests,
                "concurrency": concurrency,
                "total_time_sec": round(total_time, 2),
                "throughput_req_sec": round(total_requests / total_time, 2) if total_time > 0 else 0,
                "p50_ms": round(latencies[int(len(latencies)*0.5)] * 1000, 2) if latencies else 0,
                "p95_ms": round(latencies[int(len(latencies)*0.95)] * 1000, 2) if latencies else 0,
                "p99_ms": round(latencies[int(len(latencies)*0.99)] * 1000, 2) if latencies else 0,
                "avg_ms": round((sum(latencies)/len(latencies))*1000, 2) if latencies else 0
            }
            
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps(stats, indent=2).encode('utf-8'))
        elif self.path == '/run-demo2':
            self.send_response(200)
            self.send_header('Content-Type', 'text/plain; charset=utf-8')
            # Disable caching
            self.send_header('Cache-Control', 'no-cache')
            self.send_header('X-Content-Type-Options', 'nosniff')
            self.end_headers()
            
            # Run demo-hybrid-reactive
            process = subprocess.Popen(
                ['mvn', '-pl', 'demo-hybrid-reactive', 'exec:java'], 
                stdout=subprocess.PIPE, 
                stderr=subprocess.STDOUT,
                text=True,
                bufsize=1 # Line buffered
            )
            for line in process.stdout:
                try:
                    self.wfile.write(line.encode('utf-8'))
                    self.wfile.flush()
                except Exception:
                    break
            process.wait()
        else:
            return http.server.SimpleHTTPRequestHandler.do_GET(self)

with socketserver.TCPServer(("", PORT), MyHandler) as httpd:
    print("Serving at port", PORT)
    httpd.serve_forever()
