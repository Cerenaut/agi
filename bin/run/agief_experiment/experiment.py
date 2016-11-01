import utils
import os
import datetime
import shutil
import subprocess


class Experiment:

    log = False

    prefix = None
    prefix_delimiter = None
    experiments_def_filename = None     # the filename of the experiment definition (usually experiments.json)

    # environment variables
    agi_run_home = "AGI_RUN_HOME"
    agi_home = "AGI_HOME"
    agi_data_home = "AGI_DATA_HOME"
    variables_file = "VARIABLES_FILE"

    def __init__(self, log, prefix, prefix_delimiter):
        self.log = log
        self.prefix = prefix
        self.prefix_delimiter = prefix_delimiter

    def info(self):

        print "=============================================="
        print "Experiment Information"
        print "=============================================="

        print "Datetime: " + datetime.datetime.now().strftime("%y %m %d - %H %M")
        print "Folder: " + self.experiment_folder()
        print "Githash: " + self.githash()
        print "Variables file: " + self.variables_filepath()
        print "Prefix: " + self.prefix
        print "=============================================="

    def filepath_from_env_variable(self, filename, path_env):

        variables_file = self.variables_filepath()

        if variables_file is "" or variables_file is None:
            print "WARNING: unable to locate variables file." \

        if self.log:
            print "experiment:filepath_from_env_variable: variables file = " + variables_file

        cmd = "source " + variables_file + " && echo $" + path_env
        output, error = subprocess.Popen(cmd,
                                         shell=True,
                                         stdout=subprocess.PIPE,
                                         stderr=subprocess.PIPE,
                                         executable="/bin/bash").communicate()

        file_path = utils.cleanpath(output, filename)
        return file_path

    def githash(self):
        """ return githash of experiment-definitions """

        folder = self.experiment_folder()
        cmd = "cd " + folder + " && git rev-parse --short HEAD"

        commit, error = subprocess.Popen(cmd,
                                         shell=True,
                                         stdout=subprocess.PIPE,
                                         stderr=subprocess.PIPE,
                                         executable="/bin/bash").communicate()

        return commit

    def inputfile(self, filename):
        """ return the full path to the inputfile specified by simple filename (AGI_RUN_HOME/input/filename) """
        return self.filepath_from_env_variable("input/" + filename, self.agi_run_home)

    def outputfile(self, filename):
        """ return the full path to the output file specified by simple filename (AGI_RUN_HOME/output/filename) """
        return self.filepath_from_env_variable("output/" + filename, self.agi_run_home)

    def datafile(self, filename):
        return self.filepath_from_env_variable(filename, self.agi_data_home)

    def experiment_def_file(self):
        """ return the full path to the experiments definition file """
        return self.filepath_from_env_variable(self.experiments_def_filename, self.agi_run_home)

    def experiment_folder(self):
        """ return the full path to the experiments folder """
        return self.filepath_from_env_variable("", self.agi_run_home)

    def experimentfile(self, filename):
        """ return the full path to a file in the folder AGI_RUN_HOME """
        return self.filepath_from_env_variable(filename, self.agi_run_home)

    def entity_with_prefix(self, entity_name):
        if self.prefix is None or self.prefix is "":
            return entity_name
        else:
            return self.prefix + self.prefix_delimiter + entity_name

    def reset_prefix(self):
        use_prefix_file = False
        if use_prefix_file:
            prefix_filepath = self.filepath_from_env_variable('prefix.txt', self.agi_run_home)

            if not os.path.isfile(prefix_filepath):
                print """WARNING ****   no prefix.txt file could be found,
                      using the default root entity name: 'experiment'"""
                return None

            with open(prefix_filepath, 'r') as myfile:
                self.prefix = myfile.read()
        else:
            self.prefix = datetime.datetime.now().strftime("%y%m%d-%H%M")

    def create_input_files(self, template_prefix, baseentity_filename, basedata_filename):
        """
        Duplicate input files appending prefix to name of new file,
        and change contents of entities to use the generated prefix

        :param baseentity_filename:
        :param basedata_filename:
        :return:
        """

        self.reset_prefix()

        entity_filename = utils.append_before_ext(baseentity_filename, "_" + self.prefix)
        data_filename = utils.append_before_ext(basedata_filename, "_" + self.prefix)

        # create new input files with prefix in the name
        shutil.copyfile(self.inputfile(baseentity_filename), self.inputfile(entity_filename))
        shutil.copyfile(self.inputfile(basedata_filename),   self.inputfile(data_filename))

        # search replace contents for PREFIX and replace with 'prefix'
        utils.replace_in_file(template_prefix, self.prefix, self.inputfile(entity_filename))
        utils.replace_in_file(template_prefix, self.prefix, self.inputfile(data_filename))

        return entity_filename, data_filename

    def variables_filepath(self):
        """ return full filename with path, of the file being used for the variables file """
        dir_path = os.path.dirname(os.path.realpath(__file__))
        variables_file = os.getenv(self.variables_file, dir_path + '/../../variables.sh')
        return variables_file

