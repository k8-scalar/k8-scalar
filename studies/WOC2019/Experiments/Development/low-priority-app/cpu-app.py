import math
import threading
import time
import os

def aggressive_cpu_task():
    """Very CPU-intensive task with minimal sleeping"""
    while True:
        # Intensive mathematical computations
        result = 0
        for i in range(500000):  # Increased iterations
            # Multiple complex calculations
            result += math.sqrt(i) * math.sin(i) * math.cos(i) * math.log(i + 1)
            result += math.exp(math.sin(i)) * math.tan(i + 1)
        
        # Very short sleep to maintain high load
        time.sleep(0.01)

def high_cpu_load():
    """Generate high CPU load using most available cores"""
    num_cores = os.cpu_count()
    # Use 75% of available cores for high load
    num_threads = max(2, int(num_cores * 0.75))
    
    print(f"Starting high CPU load with {num_threads} threads on {num_cores} cores")
    print("This will consume significant CPU resources!")
    print("Press Ctrl+C to stop\n")
    
    threads = []
    for i in range(num_threads):
        thread = threading.Thread(target=aggressive_cpu_task)
        thread.daemon = True
        thread.start()
        threads.append(thread)
        print(f"Started aggressive thread {i+1}")
    
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\nStopping high CPU load...")

if __name__ == "__main__":
    high_cpu_load()
