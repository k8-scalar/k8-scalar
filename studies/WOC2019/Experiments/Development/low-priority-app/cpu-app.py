import math
import threading
import time

def cpu_intensive():
    while True:
        total = 0
        for i in range(1500000):
            total += math.sqrt(i) * math.sin(i) * math.cos(i)
        time.sleep(0.002)

# Start 8 threads immediately
for i in range(8):
    threading.Thread(target=cpu_intensive, daemon=True).start()

print("High CPU load running with 8 threads - check your top command!")
print("Press Ctrl+C to stop")

try:
    while True:
        time.sleep(1)
except:
    pass

