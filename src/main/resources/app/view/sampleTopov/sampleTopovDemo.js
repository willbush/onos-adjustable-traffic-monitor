/*
 * Copyright 2015-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 Sample Demo module. This contains the "business logic" for the topology
 overlay that we are implementing.
 */

(function () {
    'use strict';

    // injected refs
    var $log, fs, flash, wss, ds;

    // constants
    var displayStart = 'sampleTopovDisplayStart',
        displayUpdate = 'sampleTopovDisplayUpdate',
        displayStop = 'sampleTopovDisplayStop',
        thresholdUpdate = 'trafficThresholdUpdate';

    var KBps_thresholds = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
        "100", "1000", "10000", "100000", "1000000", "10000000"];

    // internal state
    var current_threshold = '0';
    var currentMode = null;

    var dialogId = 'threshold-dialog',
        dialogOptions = {
            edge: 'left'
        };

    // === ---------------------------
    // === Helper functions

    function sendDisplayStart(mode) {
        wss.sendEvent(displayStart, {
            mode: mode
        });
    }

    function sendDisplayUpdate(what) {
        wss.sendEvent(displayUpdate, {
            id: what ? what.id : ''
        });
    }

    function sendDisplayStop() {
        wss.sendEvent(displayStop);
    }

    function sendThresholdUpdate() {
        wss.sendEvent(thresholdUpdate, {
            threshold: current_threshold
        });
    }

    function selectThreshold() {
        current_threshold = KBps_thresholds[this.options[this.selectedIndex].value];
        $log.info('Selected threshold', current_threshold);
    }

    function getThresholdListContent() {
        var content = ds.createDiv(),
            form = content.append('form');

        var items = form.append('select').on('change', selectThreshold);

        for (var i = 0; i < KBps_thresholds.length; ++i) {
            var thresholdAtI = KBps_thresholds[i];
            var description = ' KBps threshold';
            var text = thresholdAtI.concat(description);

            items.append('option')
                .attr('value', i)
                .attr('selected', (thresholdAtI === current_threshold) ? true : null)
                .text(text);
        }
        return content;
    }

    function dOk() {
        // setThreshold({thres_id: threshold.thres_id});
        sendThresholdUpdate();
        $log.debug('Threshold Dialog OK button clicked')
    }

    function dClose() {
        $log.debug('Threshold Dialog Close button clicked')
    }

    // === ---------------------------
    // === Main API functions

    function startDisplay(mode) {
        if (currentMode === mode) {
            $log.debug('(in mode', mode, 'already)');
        } else {
            currentMode = mode;
            sendDisplayStart(mode);
            flash.flash('Starting display mode: ' + mode);
        }
    }

    function updateDisplay(m) {
        if (currentMode) {
            sendDisplayUpdate(m);
        }
    }

    function stopDisplay() {
        if (currentMode) {
            currentMode = null;
            sendDisplayStop();
            flash.flash('Canceling display mode');
            return true;
        }
        return false;
    }

    function updateThreshold() {
        ds.openDialog(dialogId, dialogOptions)
            .setTitle('Set Traffic Threshold')
            .addContent(getThresholdListContent())
            .addOk(dOk, 'OK')
            .addCancel(dClose, 'Close')
            .bindKeys();
    }

    // === ---------------------------
    // === Module Factory Definition

    angular.module('ovSampleTopov', [])
        .factory('SampleTopovDemoService',
            ['$log', 'FnService', 'FlashService', 'WebSocketService', 'DialogService',

                function (_$log_, _fs_, _flash_, _wss_, _ds_) {
                    $log = _$log_;
                    fs = _fs_;
                    flash = _flash_;
                    wss = _wss_;
                    ds = _ds_;

                    return {
                        startDisplay: startDisplay,
                        updateDisplay: updateDisplay,
                        stopDisplay: stopDisplay,
                        updateThreshold: updateThreshold
                    };
                }]);
}());
