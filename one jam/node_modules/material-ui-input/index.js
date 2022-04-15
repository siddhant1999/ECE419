(function ($) {

    $.fn.materialUiInput = function () {

        $(this).append("<label class='material-input-label'></label><input type='text' class='material-input'/>");

        var labelValue = $(this).attr('label');
        labelValue = labelValue ? labelValue : "Label";

        var inputElement$ = $(this).find('.material-input');
        var labelElement$ = $(this).find('.material-input-label');

        var requiredAttributeValue = $(this).attr("required");

        if (requiredAttributeValue) {
            inputElement$.attr("required", requiredAttributeValue);
        }

        labelElement$.text(labelValue);

        $(this).css({
            "position": "relative"
        });
        labelElement$.css({
            "position": "absolute",
            "top": "28px",
            "font-family": "'roboto', sans-serif",
            "-webkit-transition": "all 0.3s ease-in-out",
            "transition": "all 0.3s ease-in-out",
            "font-size": "16px",
            "color": "darkgrey",
            "cursor": "auto"
        });

        inputElement$.css({
            "width": "200px",
            "display": "block",
            "border": "none",
            "padding": "30px 0 5px 0",
            "border-bottom": "solid 1px lightgrey",
            "-webkit-transition": "all 0.3s cubic-bezier(0.64, 0.09, 0.08, 1)",
            "transition": "all 0.3s cubic-bezier(0.64, 0.09, 0.08, 1)",
            "background": "-webkit-linear-gradient(top, rgba(255, 255, 255, 0) 95%, #53cc74 5%)",
            "background": "linear-gradient(to bottom, rgba(255, 255, 255, 0) 95%, #53cc74 5%)",
            "background-position": "-200px 0",
            "background-size": "0 100%",
            "background-repeat": "no-repeat",
            "color": "black",
            "outline": "none",
            "font-size": "16px"
        });

        $(this).find('.material-input:focus').css({
            "box-shadow": "none",
            "outline": "none",
            "background-position": "0 0",
            "background-size": "200px 100%"
        });

        $(this).find('.material-input:valid').css({
            "box-shadow": "none",
            "outline": "none",
            "background-position": "0 0",
            "background-size": "200px 100%"
        });

        labelElement$.click(function () {
            inputElement$.focus();
        });
        inputElement$.focus(function () {
            var parentEl = $(this).parent();
            $(parentEl).children(".material-input-label").css({
                "top": "10px",
                "color": "#53cc74",
                "font-size": "12px"
            });
            $(parentEl).children("input").css({
                "background-position": "0 0",
                "background-size": "200px 100%"
            });
        }).blur(function () {
            var parentEl = $(this).parent();
            $(parentEl).children("input").css({
                "background-position": "-200px 0",
                "background-size": "0 100%"
            });
            if ($(this).val()) {
                $(parentEl).children(".material-input-label").css({
                    "top": "10px",
                    "color": "darkgrey",
                    "font-size": "12px"
                });
                return;
            }
            $(parentEl).children(".material-input-label").css({
                "top": "28px",
                "color": "darkgrey",
                "font-size": "16px"
            });
        });
    };

    $.fn.getInputValue = function () {
        return $(this).find('.material-input').val();
    };

}(jQuery));

