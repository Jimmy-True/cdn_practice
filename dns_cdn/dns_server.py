import socket
from dnslib import DNSRecord, DNSHeader, RR, A, QTYPE

# Set up the DNS server belongs to the CDN provider
class DNSServer:
    def __init__(self, host='0.0.0.0', port=8053):
        self.host = host
        self.port = port

    def start(self):
        # Create a UDP socket
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        sock.bind((self.host, self.port))
        print(f"DNS Server is running on {self.host}:{self.port}")

        while True:
            # Receive a DNS query
            data, addr = sock.recvfrom(512)  # buffer size is 512 bytes
            print(f"Received query from {addr}")
            self.handle_query(data, addr, sock)

    def handle_query(self, data, addr, sock):
        # Parse the DNS query
        request = DNSRecord.parse(data)
        response = DNSRecord(
            DNSHeader(id=request.header.id, qr=1, aa=1),
            q=request.q
        )

        if str(request.q.qname) == "cdn.test.cdn.chu.":
            # Create an A record
            response.add_answer(RR(request.q.qname, QTYPE.A, rdata=A("127.0.0.1"), ttl=300))

        sock.sendto(response.pack(), addr)
        print(f"Sent response to {addr}")

if __name__ == "__main__":
    server = DNSServer()
    server.start()
