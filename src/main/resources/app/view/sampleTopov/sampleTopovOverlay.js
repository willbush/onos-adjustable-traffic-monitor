// sample topology overlay - client side
//
// This is the glue that binds our business logic
// to the overlay framework.

(function () {
    'use strict';

    // injected refs
    var $log, stds;

    // internal state should be kept in the service module (not here)

    // our overlay definition
    var overlay = {
        // NOTE: this must match the ID defined in AppUiTopovOverlay
        overlayId: 'traffic-monitor-overlay',
        glyphId: '*star4',
        tooltip: 'Traffic Monitor Topo Overlay',

        // These glyphs get installed using the overlayId as a prefix.
        // e.g. 'star4' is installed as 'meowster-overlay-star4'
        // They can be referenced (from this overlay) as '*star4'
        // That is, the '*' prefix stands in for 'meowster-overlay-'
        glyphs: {
            star4: {
                vb: '0 0 8 8',
                d: 'M1,4l2,-1l1,-2l1,2l2,1l-2,1l-1,2l-1,-2z'
            },
            banner: {
                vb: '0 0 6 6',
                d: 'M1,1v4l2,-2l2,2v-4z'
            }
        },

        activate: function () {
            $log.debug("Sample topology overlay ACTIVATED");
        },
        deactivate: function () {
            stds.stopDisplay();
            $log.debug("Sample topology overlay DEACTIVATED");
        },

        // Key bindings for traffic overlay buttons
        // NOTE: fully qual. button ID is derived from overlay-id and key-name
        keyBindings: {
            0: {
                cb: function () {
                    stds.stopDisplay();
                },
                tt: 'Cancel all port traffic monitoring',
                gid: 'xMark'
            },
            V: {
                cb: function () {
                    stds.updateThreshold();
                },
                tt: 'Set Traffic Threshold',
                gid: '*banner'
            },
            F: {
                cb: function () {
                    stds.startDisplay('monitor');
                },
                tt: 'Start all port traffic monitoring',
                gid: 'chain'
            },
            G: {
                cb: buttonCallback,
                tt: 'Uses the G key',
                gid: 'crown'
            },

            _keyOrder: [
                '0', 'V', 'F', 'G'
            ]
        }
    };

    function buttonCallback(x) {
        $log.debug('Toolbar-button callback', x);
    }

    // invoke code to register with the overlay service
    angular.module('ovSampleTopov')
        .run(['$log', 'TopoOverlayService', 'SampleTopovDemoService',

            function (_$log_, _tov_, _stds_) {
                $log = _$log_;
                stds = _stds_;
                _tov_.register(overlay);
            }]);

}());
