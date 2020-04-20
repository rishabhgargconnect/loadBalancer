import asyncio
import requests
import time
from multiprocessing.dummy import Pool



url = 'http://localhost:9090/api/student/detail?name=Rishabh'
headers = {

    'Content-Type': 'text/plain',
}

# loop = asyncio.get_event_loop()
start_time = time.time()
# for i in range(1, 201):
#     loop.run_in_executor(None,requests.get(url, headers=headers))
#     # async..get(url,headers=headers)
#     # print("response = ",response.text)
# @asyncio.coroutine
# def do_checks():
#     print("hello")
#     loop = asyncio.get_event_loop()
#     req = loop.run_in_executor(None, requests.get, url)
#     #removing below sends async req
#     resp = yield from req
#     print("time taken = ",time.time() - start_time)
#     # print(resp.status_code)


# for i in range(1, 1001):
#     # loop = asyncio.get_event_loop()
#     # print("hi")
#     loop = asyncio.get_event_loop()
#     loop.run_until_complete(do_checks())


#Using futures
pool = Pool(500)
for i in range(10):
    futures = []

    for x in range(500):
        futures.append(pool.apply_async(requests.get, [url]))

    for future in futures:
        response = future.get()
        if(response.status_code!=200):
            print("500")

    print("time taken = ",time.time() - start_time)