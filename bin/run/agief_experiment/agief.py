import requests
import time
import json
import dpath.util

import utils


class AGIEF:

    log = False
    base_url = None

    def __init__(self, log, base_url):
        self.log = log
        self.base_url = base_url

    # Return when the the config parameter has achieved the value specified
    # entity = name of entity, param_path = path to parameter, delimited by '.'
    def wait_till_param(self, entity_name, param_path, value):
        wait_period = 10
        age = None
        i = 0

        print "... Waiting for param to achieve value (try every " + str(wait_period) + "s): " + entity_name + \
              "." + param_path + " = " + str(value)

        while True:
            i += 1

            age_string = ""
            if age is not None:
                age_string = ", " + entity_name + ".age = " + str(age)

            # if not self.log:
            #     utils.restart_line()
            print "Iteration = [%d]%s" % (i, age_string)    # add a comma at the end to remove newline

            try:
                param_dic = {'entity': entity_name}
                r = requests.get(self.base_url + '/config', params=param_dic)

                if self.log:
                    print "LOG: Get config: /config with params " + json.dumps(param_dic) + ", response = ", r
                    print "  LOG: response text = ", r.text
                    print "  LOG: url: ", r.url

                if r.json()['value'] is not None:
                    age = dpath.util.get(r.json(), 'value.age', '.')
                    parameter = dpath.util.get(r.json(), 'value.' + param_path, '.')
                    if parameter == value:
                        if self.log:
                            print "LOG: ... parameter: " + entity_name + "." + param_path + ", has achieved value: " + \
                                  str(value) + "."
                        break
            except requests.exceptions.ConnectionError:
                print "Oops, ConnectionError exception"
            except requests.exceptions.RequestException:
                print "Oops, request exception"

            time.sleep(wait_period)  # sleep for n seconds

        print "   -> success, parameter reached value"

    # setup the running instance of AGIEF with the input files
    def import_experiment(self, entity_filepath=None, data_filepath=None):
        print "....... Import Experiment"
        with open(entity_filepath, 'rb') as entity_data_file:
            with open(data_filepath, 'rb') as data_data_file:
                files = {'entity-file': entity_data_file, 'data-file': data_data_file}
                response = requests.post(self.base_url + '/import', files=files)
                if self.log:
                    print "LOG: Import entity file, response = ", response
                    print "  LOG: response text = ", response.text
                    print "  LOG: url: ", response.url
                    print "  LOG: post body = ", files

    def run_experiment(self, exp):

        print "....... Run Experiment"

        payload = {'entity': exp.entity_with_prefix('experiment'), 'event': 'update'}
        response = requests.get(self.base_url + '/update', params=payload)
        if self.log:
            print "LOG: Start experiment, response = ", response

        # wait for the task to finish (poll API for 'Terminated' config param)
        self.wait_till_param(exp.entity_with_prefix('experiment'), 'terminated', True)

    def export_root_entity(self, filepath, root_entity, export_type):
        payload = {'entity': root_entity, 'type': export_type}
        response = requests.get(self.base_url + '/export', params=payload)
        if self.log:
            # print "LOG: Export entity file, response text = ", response.text
            print "  LOG: response = ", response
            print "  LOG: response url = ", response.url

        # write back to file
        output_json = response.json()
        utils.create_folder(filepath)
        with open(filepath, 'w') as data_file:
            data_file.write(json.dumps(output_json, indent=4))

    # Export the full experiment state from the running instance of AGIEF
    # that consists of entity graph and the data
    def export_experiment(self, root_entity, entity_filepath, data_filepath):
        print "....... Export Experiment"
        if self.log:
            print "Exporting data for root entity: " + root_entity

        self.export_root_entity(entity_filepath, root_entity, 'entity')
        self.export_root_entity(data_filepath, root_entity, 'data')

    def wait_up(self):
        wait_period = 3

        print "....... Wait till framework has started (try every " + str(wait_period) + " seconds),   at = " \
              + self.base_url

        version = None
        i = 0
        while True:
            i += 1
            if i > 120:
                print "\nError: could not start framework, cannot continue."
                exit()

            version = self.version(True)

            if version is None:
                # utils.restart_line()
                print "Iteration = [%d / 120]" % i      # add comma at the end to remove newline
                time.sleep(wait_period)
            else:
                break

        print "\n  - framework is up, running version: " + version

    def terminate(self):
        print "...... Terminate framework"
        response = requests.get(self.base_url + '/stop')

        if self.log:
            print "LOG: response text = ", response.text

    def set_parameter_db(self, entity_name, param_path, value):
        """
        Set parameter at 'param_path' for entity 'entity_name', in the DB
        'entity_name' is the fully qualified name WITH the prefix
        """

        payload = {'entity': entity_name, 'path': param_path, 'value': value}
        response = requests.post(self.base_url + '/config', params=payload)
        if self.log:
            print "LOG: set_parameter, response = ", response

    def set_parameter_inputfile(self, entity_filepath, entity_name, param_path, value):
        """
        Set parameter at 'param_path' for entity 'entity_name', in the input file specified by 'entity_filepath'
        'entity_name' is the fully qualified name WITH the prefix

        :param entity_filepath:
        :param entity_name:
        :param param_path:
        :param value:
        :return:
        """

        log_debug = False

        print "Set Parameter: ", entity_name + "." + param_path + " = " + str(value)

        if log_debug:
            print "LOG: in file: " + entity_filepath

        # open the entity input file
        with open(entity_filepath) as data_file:
            data = json.load(data_file)

        # get the first element in the array with dictionary field "entity-name" = entity_name
        entity = dict()
        for entity_i in data:
            if not entity_i["name"] == entity_name:
                continue
            entity = entity_i
            break

        if not entity:
            print "ERROR: Could not find an entity in the input file matching the entity name specified in the " \
                  "experiment file in field 'file-entities'."
            print "\tEntity input file: " + entity_filepath
            print "\tEntity name: " + entity_name
            print "CANNOT CONTINUE"
            exit()

        # get the config field, and turn it into valid JSON
        config_str = entity["config"]

        if log_debug:
            print "LOG: Raw configStr   = " + config_str

        # configStr = configStr.replace("\\\"", "\"")       --> don't need this anymore, depends on python behaviour
        config = json.loads(config_str)

        if log_debug:
            print "LOG: config(t)   = " + json.dumps(config, indent=4)

        dpath.util.set(config, param_path, value, '.')

        if log_debug:
            print "LOG: config(t+1) = " + json.dumps(config, indent=4)

        # put the escape characters back in the config str and write back to file
        config_str = json.dumps(config)
        # configStr = configStr.replace("\"", "\\\"")       --> don't need this anymore, depends on python behaviour

        if log_debug:
            print "LOG: Modified configStr   = " + config_str

        entity["config"] = config_str

        # write back to file
        with open(entity_filepath, 'w') as data_file:
            data_file.write(json.dumps(data, indent=4))

    def version(self, is_suppress_console_output=False):
        """
        Find out the version from the running framework, through the RESTful API. Return the string,
        or None if it could not retrieve the version.
        """

        version = None
        try:
            response = requests.get(self.base_url + '/version')
            if self.log:
                print "LOG: response = ", response

            response_json = response.json()
            if 'version' in response_json:
                version = response_json['version']

        except requests.ConnectionError:
            version = None

            if not is_suppress_console_output:
                print "Error connecting to agief to retrieve the version."

        return version


