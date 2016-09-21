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
        while True:
            try:
                param_dic = {'entity': entity_name}
                r = requests.get(self.base_url + '/config', params=param_dic)

                if self.log:
                    print "LOG: Get config: /config with params " + json.dumps(param_dic) + ", response = ", r
                    print "  LOG: response text = ", r.text
                    print "  LOG: url: ", r.url

                if r.json()["value"] is not None:
                    parameter = dpath.util.get(r.json(), "value." + param_path, '.')
                    if parameter == value:
                        if self.log:
                            print "LOG: ... parameter: " + entity_name + "." + param_path + ", has achieved value: " + str(
                                value) + "."
                        break
            except requests.exceptions.ConnectionError:
                print "Oops, ConnectionError exception"
            except requests.exceptions.RequestException:
                print "Oops, request exception"

            if self.log:
                print "  LOG: ... parameter: " + entity_name + "." + param_path + ", has not achieved value: " + \
                      str(value) + ",   wait 2s and try again ........"
            time.sleep(2)  # sleep for n seconds

    # setup the running instance of AGIEF with the input files
    def import_experiment(self, entity_filepath=None, data_filepath=None):
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
            print "LOG: Export entity file, response text = ", response.text
            print "  LOG: response url = ", response.url

        # write back to file
        output_json = response.json()
        utils.create_folder(filepath)
        with open(filepath, 'w') as data_file:
            data_file.write(json.dumps(output_json, indent=4))

    # Export the full experiment state from the running instance of AGIEF
    # that consists of entity graph and the data
    def export_experiment(self, root_entity, entity_filepath, data_filepath):

        if self.log:
            print "Exporting data for root entity: " + root_entity

        self.export_root_entity(entity_filepath, root_entity, 'entity')
        self.export_root_entity(data_filepath, root_entity, 'data')

    def wait_up(self):
        print "....... wait till framework has started at = " + self.base_url

        version = "** could not parse version number **"
        while True:
            try:
                response = requests.get(self.base_url + '/version')
                if self.log:
                    print "LOG: response = ", response

                response_json = response.json()
                if 'version' in response_json:
                    version = response_json['version']
                break
            except requests.ConnectionError:
                time.sleep(1)
                print "  - no connection yet ......"

        print "  - framework is up, running version: " + version

    def terminate(self):
        print "...... terminate framework"
        response = requests.get(self.base_url + '/stop')

        if self.log:
            print "LOG: response text = ", response.text

    def set_parameter(self, entity_filepath, entity_name, param_path, val):
        """
        Set parameter at 'param_path' for entity 'entity_name', in the input file specified by 'entity_filepath'
        'entity_name' is the fully qualified name WITH the prefix

        :param entity_filepath:
        :param entity_name:
        :param param_path:
        :param val:
        :return:
        """

        log_debug = False

        print "Modify Parameters: ", entity_name + "." + param_path + " = " + str(val)
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

        if self.log:
            print "LOG: config(t)   = " + json.dumps(config, indent=4)

        dpath.util.set(config, param_path, val, '.')

        if self.log:
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
