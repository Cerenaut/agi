import utils
import os

class Experiment:
    prefix = None

    def __init__(self):
        self.prefix = self.entity_prefix()

    # return the full path to the inputfile specified by simple filename (AGI_RUN_HOME/input/filename)
    def inputfile(self, filename):
        return utils.filepath_from_env_variable("input/" + filename, "AGI_RUN_HOME")

    # return the full path to the output file specified by simple filename (AGI_RUN_HOME/output/filename)
    def outputfile(self, filename):
        return utils.filepath_from_env_variable("output/" + filename, "AGI_RUN_HOME")

    # return None if prefix.txt file does not exist
    def entity_prefix(self):
        prefix_filepath = utils.filepath_from_env_variable('prefix.txt', 'AGI_RUN_HOME')

        if not os.path.isfile(prefix_filepath):
            print "WARNING ****   no prefix.txt file could be found, using the default root entity name: 'experiment'"
            return None

        with open(prefix_filepath, 'r') as myfile:
            prefix = myfile.read()
            return prefix

    def entity_with_prefix(self, entity_name):
        if self.prefix is None or self.prefix is "":
            return entity_name
        else:
            return self.prefix + "/" + entity_name
