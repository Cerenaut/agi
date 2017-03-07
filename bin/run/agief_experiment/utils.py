import subprocess
import os
import errno
import fileinput
import sys
import time


def restart_line():
    sys.stdout.write('\r')
    sys.stdout.flush()


def replace_in_file(src_string, dest_string, file_path):
    # NOTE: cannot use context manager (i.e. 'with') with FileInput in 2.7
    f = fileinput.FileInput(file_path, inplace=True)
    for line in f:
        line = line.replace(src_string, dest_string).rstrip()
        print line
    f.close()


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


def filepath_from_env_variable(filename, path_env):
    cmd = "echo $" + path_env
    output, error = subprocess.Popen(cmd,
                                     shell=True,
                                     stdout=subprocess.PIPE,
                                     stderr=subprocess.PIPE,
                                     executable="/bin/bash").communicate()

    file_path = cleanpath(output, filename)
    return file_path


def cleanpath(path, filename):
    """ given a path and a filename, return the fully qualified filename with path"""

    path_from_env = path.strip()
    filename = filename.strip()
    filename = filename.lstrip('/')
    file_path = os.path.join(path_from_env, filename)
    return file_path


def run_bashscript_repeat(cmd, max_repeats, wait_period, verbose=False):
    """ Run a shell command repeatedly until exit status shows success.
    Run command 'cmd' a maximum of 'max_repeats' times and wait 'wait_period' between attempts. """

    if verbose:
        print "run_bashscript_repeat, running cmd = " + cmd

    success = False
    for i in range(1, max_repeats + 1):
        child = subprocess.Popen(cmd,
                                 shell=True,
                                 stdout=subprocess.PIPE,
                                 stderr=subprocess.PIPE,
                                 executable="/bin/bash")

        output, error = child.communicate()  # get the outputs. NOTE: This will block until shell command returns.

        exit_status = child.returncode

        if verbose:
            print "Stdout: " + output
            print "Exit status: " + str(exit_status)

        print "utils.run_bashscript_repeat - stderr: " + error

        if exit_status == 0:
            success = True
            break

        print "Run bash script was unsuccessful on attempt " + str(i)
        print "Wait " + str(wait_period) + ", and try again."

        time.sleep(wait_period)

    if not success:
        print "ERROR: was not able run shell command: " + cmd
        print " Exit status = " + str(exit_status)
        exit(1)


def check_validity(files, log=False):
    """ Check validity of files, and exit if they do not exist or not specified """

    is_valid = True
    file_paths = []

    for file in files:
        if os.path.isfile(file) and os.path.exists(file):       # TODO for some reason exists() not working
            file_paths.append(file)
        else:
            is_valid = False
            print "ERROR: check_validity(), this file is not valid: " + file
            break

    return is_valid
