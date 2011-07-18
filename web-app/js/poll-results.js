var chart;
$(document).ready(function() {
    var ownerId = $("#ownerId").val();
    $.getJSON("plot/"+ownerId, function(data){
        data = data || [];
        var labels = $.map(data, function(element) {
            return element.value;
        })|| [];
        var values = $.map(data, function(element) {
            return element.percent;
        })    || [];
        chart = new Highcharts.Chart({
                    chart: {
                        renderTo: 'container',
                        defaultSeriesType: 'bar'
                    },
                    title: {
                        text: 'Poll Results'
                    },
                    xAxis: {
                        categories: labels
                    },
                    yAxis: {
                        min: 0,
                        title: {
                            text: 'Percentage'
                        }
                    },
//                    legend: {
//                        backgroundColor: '#FFFFFF',
//                        reversed: true
//                    },
                    tooltip: {
                        formatter: function() {
                            return '' + this.x +' '+ this.y + '';
                        }
                    },
                    plotOptions: {
                        series: {
                            stacking: 'normal'
                        }
                    },
                    series: [
                        {
                            data: values
                        }
                    ]
                });

    });

});
