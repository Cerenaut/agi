#!/bin/bash

sudo -u postgres psql -f $AGI_HOME/experimental-framework/resources/sql/agidb.sql
