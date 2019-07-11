import React, { Component } from "react";

import { ICON_PATHS, loadIcon, parseViewBox } from "metabase/icon_paths";
import Icon from "metabase/components/Icon";

export default class AllIcons extends Component {

  render() {
    return (
      <div className="p4">
        <h1 className="mb2">All Icons</h1>

        {Object.keys(ICON_PATHS).map(key => 
          <div className="p1 text-centered inline-block" style={{ width: "120px" }}>
            <Icon name={key} size={20} />
            <p>{key}</p>
          </div>
        )}
      </div>
    );
  }
}
