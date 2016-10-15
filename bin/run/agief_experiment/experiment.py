import utils
import os
import datetime
import shutil


class Experiment:
    prefix = None
    prefix_delimiter = None

    def __init__(self, prefix, prefix_delimiter):
        self.prefix = prefix
        self.prefix_delimiter = prefix_delimiter

    # return the full path to the inputfile specified by simple filename (AGI_RUN_HOME/input/filename)
    def inputfile(self, filename):
        return utils.filepath_from_env_variable("input/" + filename, "AGI_RUN_HOME")

    # return the full path to the output file specified by simple filename (AGI_RUN_HOME/output/filename)
    def outputfile(self, filename):
        return utils.filepath_from_env_variable("output/" + filename, "AGI_RUN_HOME")

    def entity_with_prefix(self, entity_name):
        if self.prefix is None or self.prefix is "":
            return entity_name
        else:
            return self.prefix + self.prefix_delimiter + entity_name

    def reset_prefix(self):
        use_prefix_file = False
        if use_prefix_file:
            prefix_filepath = utils.filepath_from_env_variable('prefix.txt', 'AGI_RUN_HOME')

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
