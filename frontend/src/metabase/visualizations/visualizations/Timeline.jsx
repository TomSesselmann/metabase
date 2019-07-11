/* @flow */

import React, { Component } from "react";
import ReactDOM from "react-dom";
import { t } from "ttag";
import d3 from "d3";
import cx from "classnames";

import _ from "underscore";

import colors from "metabase/lib/colors";
import { formatValue } from "metabase/lib/formatting";
import { isNumeric } from "metabase/lib/schema_metadata";
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
    return rows.length === 1 && cols.length === 1;
  }

  static checkRenderable([
    {
      data: { cols, rows },
    },
  ]) {
    if (!isNumeric(cols[0])) {
      throw new Error(t`Gauge visualization requires a number.`);
    }
  }

  state = {
    mounted: false,
  };

  _label: ?HTMLElement;

  static settings = {
    ...columnSettings({
    }),
  };

  composeData(rows, cols) {
    console.log(rows, cols);

    const startTimeIndex = cols.findIndex(({name}) => name === "start_time");
    const endTimeIndex = cols.findIndex(({name}) => name === "end_time");
    const classIndex = cols.findIndex(({name}) => name === "class_type");

    let data = {};

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

    console.log("data: ", data);

    return Object.values(data);
  }

  componentDidMount() {
    this.setState({ mounted: true });
    const {
      series: [
        {
          data: { rows, cols },
        },
      ],
    } = this.props;

    const data = this.composeData(rows, cols);

    const width = this.timelineContainer.offsetWidth;
    const height = this.timelineContainer.offsetHeight;

    const chart = timeline()
      .width(width)
      .height(height)
      .stack()
      .itemHeight((height - 70) / data.length)
      .itemMargin(15)
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
    d3.select("#timeline-chart").datum(data).call(chart);
  }

  render() {
    const {
      className,
    } = this.props;
    
    return (
      <div className={cx(className, "relative p2")}>
        <div className="overflow-hidden full-height" ref={ref => this.timelineContainer = ref}>
          <svg id="timeline-chart" style={{ width: "100%" }}>
          </svg>
        </div>
      </div>
    );
  }
}