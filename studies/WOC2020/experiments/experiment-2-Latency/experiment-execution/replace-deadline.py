import fileinput
import sys
import datetime
import json
import os

def main():
    script_dir = os.path.dirname(__file__)
    currentTime = datetime.datetime.now().time()
    timer=(int(sys.argv[1]))
    file_loc = os.path.join(script_dir, 'upgrade-users.json')
    new_file_loc = os.path.join(script_dir, 'new-upgrade-users.json')
    print file_loc
    with open(file_loc, 'r') as file:
        json_data = json.load(file)
        for item in json_data:
            if item["deadline"]:
                timer+=20
                newTime= addSecs(currentTime, timer)
                item["deadline"] = "{}".format(newTime)
    with open(new_file_loc, 'w') as file:
        json.dump(json_data, file, indent=2)

def addSecs(tm, secs):
    fulldate = datetime.datetime(100, 1, 1, tm.hour, tm.minute, tm.second)
    fulldate = fulldate + datetime.timedelta(seconds=secs)
    return fulldate.time()

if __name__== "__main__":
    main()