// SPDX-FileCopyrightText: Copyright © 2025, 2026 Caleb Cushing
//
// SPDX-License-Identifier: CC0-1.0
// SPDX-License-Identifier: MIT

/** @type {import('prettier').Options} */
module.exports = {
  printWidth: 120,
  xmlWhitespaceSensitivity: "ignore",
  keySeparator: "=",
  plugins: [
    require.resolve("@prettier/plugin-xml"),
    require.resolve("prettier-plugin-properties"),
    require.resolve("prettier-plugin-java"),
    require.resolve("prettier-plugin-toml"),
  ],
};
