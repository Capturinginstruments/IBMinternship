#!/bin/bash
set -e

# Start MySQL in the background (runs the official mysql entrypoint script)
echo "Starting MySQL in the background..."
docker-entrypoint.sh mysqld &

# Wait for MySQL to boot up
echo "Waiting 15 seconds for MySQL to initialize..."
sleep 15

# Start a tiny python HTTP server on Render's assigned PORT to satisfy the web health check
PORT=${PORT:-80}
echo "Starting HTTP health check server on port $PORT..."
python3 -c "
import http.server
import socketserver
import os

PORT = int(os.environ.get('PORT', 80))
class HealthCheckHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/plain')
        self.end_headers()
        self.wfile.write(b'OK')

with socketserver.TCPServer(('', PORT), HealthCheckHandler) as httpd:
    print('Serving health check at port', PORT)
    httpd.serve_forever()
"
