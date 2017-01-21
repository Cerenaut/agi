import json

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
    agi_exp_home = "AGI_EXP_HOME"
    agi_home = "AGI_HOME"
    agi_run_home = "AGI_RUN_HOME"
    agi_data_run_home = "AGI_DATA_RUN_HOME"
    agi_data_exp_home = "AGI_EXP_HOME"
    variables_file = "VARIABLES_FILE"

    def __init__(self, log, prefix, prefix_delimiter, experiments_def_filename):
        self.logfine = False
        self.log = log
        self.prefix = prefix
        self.prefix_delimiter = prefix_delimiter
        self.experiments_def_filename = experiments_def_filename

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

    def filepath_from_exp_variable(self, filename, path_env):

        variables_file = self.variables_filepath()

        if variables_file is "" or variables_file is None:
            print "WARNING: unable to locate variables file." \

        if self.log and self.logfine:
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

    def inputfiles_for_generation(self):

        base_entity_filename, base_data_filenames = self.input_filenames_from_exp_definitions(False)

        """ Get the input files, with full path, to be generated """

        entity_filename = self.inputfile(base_entity_filename)

        data_filenames = []
        for base_data_filename in base_data_filenames:
            data_filename = self.inputfile(base_data_filename)
            data_filenames.append(data_filename)

        return entity_filename, data_filenames

    def input_filenames_from_exp_definitions(self, is_import_files):
        """ Get the input files as defined in the experiments definitions file.
        i.e. do not compute full path, do not add prefix etc.

        :param is_import_files: boolean to specify whether you want the input files for 'import' or 'generation'
        :return: entityfilename, datafilenames
        """

        exps_filename = self.experiment_def_file()

        with open(exps_filename) as exps_file:
            filedata = json.load(exps_file)

        for exp_i in filedata['experiments']:

            if is_import_files:
                key = 'import-files'
            else:
                key = 'gen-files'

            input_files = exp_i[key]  # import files dictionary

            if self.log:
                print "LOG: Input Files Dictionary = "
                print "LOG: ", json.dumps(input_files, indent=4)

            # get experiment file-names, and expand to full path
            base_entity_filename = input_files['file-entities']
            base_data_filenames = input_files['file-data']

            return base_entity_filename, base_data_filenames

    def inputfile(self, filename):
        """ return the full path to the inputfile specified by simple filename (AGI_EXP_HOME/input/filename) """
        return self.filepath_from_exp_variable("input/" + filename, self.agi_exp_home)

    def outputfile(self, filename):
        """ return the full path to the output file specified by simple filename (AGI_EXP_HOME/output/filename) """
        return self.filepath_from_exp_variable("output/" + filename, self.agi_exp_home)

    def runfolder(self, subfolder):
        """ return absolute path to a subfolder in the AGI_RUN_HOME/ folder """
        return self.filepath_from_exp_variable(subfolder, self.agi_run_home)

    def datafile(self, filename):
        """ return the file in the data folder, on the system where compute is running """
        return self.filepath_from_exp_variable(filename, self.agi_data_run_home)

    def experiment_def_file(self):
        """ return the full path to the experiments definition file """
        return self.filepath_from_exp_variable(self.experiments_def_filename, self.agi_exp_home)

    def experiment_folder(self):
        """ return the full path to the experiments folder """
        return self.filepath_from_exp_variable("", self.agi_exp_home)

    def experimentfile(self, filename):
        """ return the full path to a file in the folder AGI_EXP_HOME """
        return self.filepath_from_exp_variable(filename, self.agi_exp_home)

    def entity_with_prefix(self, entity_name):
        if self.prefix is None or self.prefix is "":
            return entity_name
        else:
            return self.prefix + self.prefix_delimiter + entity_name

    def reset_prefix(self):

        if self.log:
            print "-------------- RESET_PREFIX -------------"

        use_prefix_file = False
        if use_prefix_file:
            prefix_filepath = self.filepath_from_exp_variable('prefix.txt', self.agi_exp_home)

            if not os.path.isfile(prefix_filepath):
                print """WARNING ****   no prefix.txt file could be found,
                      using the default root entity name: 'experiment'"""
                return None

            with open(prefix_filepath, 'r') as myfile:
                self.prefix = myfile.read()
        else:
            new_prefix = datetime.datetime.now().strftime("%y%m%d-%H%M")
            if new_prefix != self.prefix:
                self.prefix = new_prefix
            else:
                self.prefix += self.prefix + "i"

    def create_input_files(self, template_prefix, baseentity_filename, basedata_filenames):
        """
        Duplicate input files appending prefix to name of new file,
        and change contents of entities to use the generated prefix

        :param baseentity_filenames: entity filename to be copied and prefix changed internally
        :param basedata_filename:  array of data filenames to be copied and prefix changed internally
        :return:
        """

        self.reset_prefix()

        baseentity_filepath = self.inputfile(baseentity_filename)

        if not os.path.isfile(baseentity_filepath):
            print "ERROR: create_input_files(): The data file does not exist" + baseentity_filepath + \
                  "\nCANNOT CONTINUE."
            exit()

        entity_filename = utils.append_before_ext(baseentity_filename, "_" + self.prefix)
        shutil.copyfile(baseentity_filepath, self.inputfile(entity_filename))   # create new input files with prefix in the name
        utils.replace_in_file(template_prefix, self.prefix, self.inputfile(entity_filename))    # search replace contents for PREFIX and replace with 'prefix'

        data_filenames = []
        for basedata_filename in basedata_filenames:
            basedata_filepath = self.inputfile(basedata_filename)

            if not os.path.isfile(basedata_filepath):
                print "ERROR: create_input_files(): The data file does not exist" + basedata_filepath + \
                      "\nCANNOT CONTINUE."
                exit()

            data_filename = utils.append_before_ext(basedata_filename, "_" + self.prefix)
            shutil.copyfile(basedata_filepath, self.inputfile(data_filename))     # create new input files with prefix in the name
            utils.replace_in_file(template_prefix, self.prefix, self.inputfile(data_filename))      # search replace contents for PREFIX and replace with 'prefix'
            data_filenames.append(data_filename)

        return entity_filename, data_filenames

    def variables_filepath(self):
        """ return full filename with path, of the file being used for the variables file """
        dir_path = os.path.dirname(os.path.realpath(__file__))
        variables_file = os.getenv(self.variables_file, dir_path + '/../../variables.sh')
        return variables_file