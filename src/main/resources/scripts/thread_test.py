import threading

# Lock pentru a gestiona scrierea concurentă în fișier
file_write_lock = threading.Lock()


def write_to_file(process_number, result):
    # Deschide sau creează fișierul în modul append ('a')
    with open('output_file.txt', 'a') as file:
        # Acquire the lock before writing to the file
        file_write_lock.acquire()
        try:
            # Scrie rezultatele în fișier
            file.write(f'Worker {process_number} result: {result}\n')
        finally:
            # Release the lock after writing to the file
            file_write_lock.release()


def worker_function(process_number):
    print(f'Worker {process_number} started')

    # Efectuează o operație în proces
    result = process_number * 2

    # Scrie rezultatul în fișier în mod sigur
    write_to_file(process_number, result)


if __name__ == "__main__":
    # Numărul de procesoare disponibile
    num_processes = 4

    # Inițializează lista de thread-uri
    threads = []

    # Creează și rulează fiecare thread
    for i in range(num_processes):
        thread = threading.Thread(target=worker_function, args=(i,))
        threads.append(thread)
        thread.start()

    # Așteaptă ca toate thread-urile să se încheie
    for thread in threads:
        thread.join()

    print("Finished writing to file.")
