FROM python:3.9-slim

# Set working directory in container
WORKDIR /app

# Copy the DNS server script into the container
COPY dns_server.py .

# Install required Python packages
RUN pip install dnslib

# Expose the port on which the DNS server will run
EXPOSE 8053

# Command to run the DNS server
CMD ["python", "dns_server.py"]
