import threading
import csv

file_write_lock = threading.Lock()
local_thread_variable = threading.local()
statistics_file = './alt_csv.txt'
local_thread_variable.thread_string= "a"

def write_to_statistics_file(chunk):
    with open(statistics_file, 'a') as file:
        file_write_lock.acquire()
        try:
            # Scrie rezultatele în fișier
            file.write(str(chunk) + '\n')
        finally:
            # Release the lock after writing to the file
            file_write_lock.release()

def process_chunk(chunk,i):
    local_thread_variable.thread_string = "Eu sun treadul " + str(i)
    write_to_statistics_file(local_thread_variable.thread_string)


# Funcția pentru fiecare thread
def worker_function(chunk, i):
    process_chunk(chunk,i)
    print("Thread ", i)


def process_csv_with_threads(csv_file, num_threads=1000):
    with open(csv_file, 'r') as csvfile:
        csv_reader = csv.reader(csvfile)
        next(csv_reader)  # Skip header
        total_rows = sum(1 for _ in csvfile)
        chunk_size = total_rows // num_threads
        remaining_rows = total_rows % num_threads
        print(chunk_size, remaining_rows)

    with open(csv_file, 'r') as csvfile:
        csv_reader = csv.reader(csvfile)
        next(csv_reader)  # Skip header

        threads = []
        for i in range(num_threads-1):
            chunk =  [next(csv_reader) for _ in range(chunk_size)]
            thread = threading.Thread(target=worker_function, args=(chunk, i))
            threads.append(thread)
            thread.start()

        # Ultimul thread primește toate rândurile rămase
        last_chunk =[next(csv_reader) for _ in range(chunk_size + remaining_rows)]
        thread = threading.Thread(target=worker_function, args=(last_chunk, num_threads - 1))
        threads.append(thread)
        thread.start()

        for thread in threads:
            thread.join()

if __name__ == "__main__":
    # Specifică numele fișierului CSV
    csv_file = 'sample-websites.csv'
    process_csv_with_threads(csv_file)
