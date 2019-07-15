/* @flow */

// Author: Thomas Sesselmann
// Date: 15/07/2019
// TODO: Add Hover functionality
// TODO: Dynamic tick intervals
// TODO: Make it work with metabase data grouping
// TODO: Add settings

import React, { Component } from "react";
import { t } from "ttag";
import d3 from "d3";
import cx from "classnames";
import { columnSettings } from "metabase/visualizations/lib/settings/column";

import timeline from "metabase/visualizations/lib/d3-timeline";

import type { VisualizationProps } from "metabase/meta/types/Visualization";

export default class Timeline extends Component {
  props: VisualizationProps;

  static uiName = t`Timeline`;
  static identifier = "timeline";
  static iconName = "filter";

  static minSize = { width: 4, height: 4 };

  static isSensible({ cols, rows }) {
    // Really basic check
    // TODO: Make this more accurate
    return cols.length > 2;
  }

  static checkRenderable([
    {
      data: { cols, rows },
    },
  ]) {
    if (!(
      cols.find(({name}) => name.toUpperCase() === "START_TIME") &&
      cols.find(({name}) => name.toUpperCase() === "END_TIME") &&
      cols.find(({name}) => name.toUpperCase() === "CLASS_TYPE")
    )) {
      throw new Error(t`Timeline visualization requires Raw data.`);
    }
    
  }

  state = {
    width: 0,
    height: 0,
  };

  static settings = {
    ...columnSettings({
    }),
  };

  composeData(rows, cols) {
    const startTimeIndex = cols.findIndex(({name}) => name.toUpperCase() === "START_TIME");
    const endTimeIndex = cols.findIndex(({name}) => name.toUpperCase() === "END_TIME");
    const classIndex = cols.findIndex(({name}) => name.toUpperCase() === "CLASS_TYPE");

    const data = {};

    for (let i = rows.length - 1; i >= 0; i--) {
      const row = rows[i];
      const classType = row[classIndex];
      if(!data[classType]) {
        data[classType] = {label: classType, times:[]};
      }
      data[classType].times.push({
        "starting_time": new Date(row[startTimeIndex]).getTime(),
        "ending_time": new Date(row[endTimeIndex]).getTime()
      })
    }

    return Object.values(data);
  }

  componentDidUpdate(prevProps, prevState) {
    const {
      series: [
        { data }
      ],
    } = this.props;

    const width = this.timelineContainer.offsetWidth;
    const height = this.timelineContainer.offsetHeight;

    console.log(data.rows, data.cols);

    if (
      prevState.width !== width ||
      prevState.height !== height ||
      prevProps.data !== data
    ) {
      this.setState({ width: width, height: height });

      const composedData = this.composeData(data.rows, data.cols);
      const margin = 15;

      const chart = timeline()
        .width(width)
        .stack()
        .itemHeight((height - (margin * composedData.length) - 25) / composedData.length)
        .itemMargin(margin)
        .margin({left:128, right:0, top:0, bottom:0})
        .tickFormat({
          format: d3.time.format("%b %e"),
          tickTime: d3.time.days,
          tickInterval: 1,
          tickSize: 6
        })
        // .hover(function(d, i, datum) {
        //     // d is the current rendering object
        //     // i is the index during d3 rendering
        //     // datum is the id object
        //     //var div = $('#hoverRes');
        //     // var colors = chart.colors();
        //     //div.find('.coloredDiv').css('background-color', colors(i))
        //     //div.find('#name').text(datum.label);
        // })
        // .click(function(d, i, datum) {
        //     //alert(datum.label);
        // })
        // .scroll(function(x, scale) {
        //     // $("#scrolled_date").text(scale.invert(x) + " to " + scale.invert(x+width));
        // });
      d3.select('#timeline-chart').remove();
      d3.select("#timeline-container")
        .append("svg")
        .attr("width", width)
        .attr("id", "timeline-chart")
        .datum(composedData)
        .call(chart);
    }
  }

  render() {
    const { className } = this.props;
    
    return (
      <div className={cx(className, "relative m2")}>
        <div id="timeline-container" className="spread" ref={ref => this.timelineContainer = ref}>
        </div>
      </div>
    );
  }
}