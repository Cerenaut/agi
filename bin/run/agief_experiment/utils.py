import subprocess
import os
import errno
import fileinput


def replace_in_file(src_string, dest_string, file_path):
    # NOTE: cannot use context manager (i.e. 'with') with FileInput in 2.7
    f = fileinput.FileInput(file_path, inplace=True)
    for line in f:
        line = line.replace(src_string, dest_string).rstrip()
        print line
    f.close()


def filepath_from_env_variable(filename, path_env):
    variables_file = os.getenv('VARIABLES_FILE', 'variables.sh')

    cmd = "source ../" + variables_file + " && echo $" + path_env
    output, error = subprocess.Popen(cmd,
                                     shell=True,
                                     stdout=subprocess.PIPE,
                                     stderr=subprocess.PIPE,
                                     executable="/bin/bash").communicate()

    path_from_env = output.strip()
    file_path = os.path.join(path_from_env, filename)
    return file_path


def create_folder(filepath):
    if not os.path.exists(os.path.dirname(filepath)):
        try:
            os.makedirs(os.path.dirname(filepath))
        except OSError as exc:  # Guard against race condition
            if exc.errno != errno.EEXIST:
                raise


def append_before_ext(filename, text):
    file_split = os.path.splitext(filename)
    new_filename = file_split[0] + text + file_split[1]
    return new_filename


def getbaseurl(host, port):
    return 'http://' + host + ':' + port

