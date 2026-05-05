from concurrent.futures import ThreadPoolExecutor
from functools import reduce
from typing import Tuple
from urllib.parse import urljoin

import requests
from bs4 import BeautifulSoup


def pipe(value, *funcs):
    return reduce(lambda acc, f: f(acc), funcs, value)


url = "https://www.football-data.co.uk/englandm.php"

response = requests.get(url)
soup = BeautifulSoup(response.text, "html.parser")

links = pipe(
    soup.find_all("a", href=True),
    lambda tags: filter(lambda a: a["href"].endswith("/E1.csv"), tags),
    lambda links: map(lambda a: urljoin(url, a["href"]), links),
    sorted,
)


def download(item: Tuple[int, str]):
    idx, url = item
    print(f"Downloading {idx}: {url}")

    r = requests.get(url, timeout=20)
    r.raise_for_status()

    filename = f"../data/E1-{idx}.csv"
    with open(filename, "wb") as f:
        f.write(r.content)
    return filename


with ThreadPoolExecutor(max_workers=4) as pool:
    res = pool.map(download, enumerate(links)),

print(list(res))
