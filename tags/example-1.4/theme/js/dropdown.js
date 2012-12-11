// CAST: Dropdown Menu

// Base structure of code according to: http://docs.jquery.com/Plugins/Authoring (as of 09/12/2012)

/*
// calls the init method
$('.navMenu').CAST_Dropdown_Menu();

// calls the init method - setting sub menu indicator and forcing mobile style actions
$('.navMenu').CAST_Dropdown_Menu({
    subMark: "--",
    isMobile : true
});

// Callback definitions
create      - menu initialization complete

*/

(function($) {
    // Mobile browser detection
    var $isMobile = false;
    if( /Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent) ) {
        $isMobile = true;
    }

    // Menu object holder
    var menu = null;

    var c = {
        // Class names
        'hasSubMenu'        : "show-sub",
        'showSubMenu'       : "show-menu",
        'showHover'         : "show-hover",
    };

    var settings = {
        // Default Settings
        'subMark'           : "&gt;",       // Text character to use to indicate presence of sub-menu (overrule in CSS for graphical one)
        'isMobile'          : $isMobile,    // Mobile browser flag
        'delayShow'         : 150,          // Delay for showing menu (not currently impemented)
        'delayHide'         : 500,          // Delay for hiding menu
    };

    var methods = {
        // Initialize
        init : function(options) {
            settings = $.extend(settings, options);

            // Set menu holder
            menu = $(this);

            // Check for sub menu items and add indicator
            $(this).find('ul').each(function() {
                $(this).closest('li').addClass(c.hasSubMenu).find('a').eq(0).append(' <span class="sub-mark">' + settings.subMark + '</span>');
            });

            // Set tabIndex=-1 so that sub-menu links can't receive focus until menu is open (skip top level items)
            $(this).find('ul a').attr('tabIndex',-1);

            // Handle link focus
            $(this).find('a').focus(function() {
                methods.showMenu(this);
            });

            // Handle menu item mouse interaction
            $(this).find('li').hover(
                function() {
                    // Mouseenter
                    if (menu.timerHide) clearTimeout(menu.timerHide);
                    methods.showMenu(this);
                },
                function() {
                    // Mouseleave
                    if (menu.timerHide) clearTimeout(menu.timerHide);
                    menu.timerHide = setTimeout(function() {
                        menu.timerHide = null;
                        methods.hideMenu();
                    }, settings.delayHide);
                 }
            );

            // Hide menu if click or focus occurs outside of navigation
            $(this).find('a').last().keydown(function(e){
                if(e.keyCode == 9) {
                    // If the user tabs out of the navigation hide all menus
                    methods.hideMenu();
                }
            });
            $(document).click(function() {
                methods.hideMenu();
            });

            $(this).click(function(e){
                e.stopPropagation();
            });

            methods._trigger('create');
        },

        showMenu : function(node) {
            $(node).closest('ul').find('.' + c.showSubMenu).removeClass(c.showSubMenu).find('a').attr('tabIndex',-1);
            $(node).closest('li').find('ul').eq(0).addClass(c.showSubMenu).find('a').attr('tabIndex',0);

            $(node).closest('ul').find('.' + c.showHover).removeClass(c.showHover);
            $(node).closest('ul').find('.' + c.showSubMenu).closest('li').addClass(c.showHover);
        },

        hideMenu : function() {
            menu.find('.' + c.showHover).removeClass(c.showHover);
            menu.find('.' + c.showSubMenu).removeClass(c.showSubMenu).find('a').attr('tabIndex',-1);
        },

        /**
         * Callback trigger function
         *
         * @param {Object} callback function name
         */
        _trigger : function(callback) {

            // Restrict informtion that is sent back
            var options = {
                'colors'            : settings.colors,
            };

            if ($.isFunction(settings[callback])) {
                settings[callback](options);
            }
        },
    };

    $.fn.CAST_Dropdown_Menu = function(method) {
        // Method calling logic
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || ! method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' +  method + ' does not exist on jQuery.CAST_Dropdown_Menu');
        }
    };

})(jQuery);