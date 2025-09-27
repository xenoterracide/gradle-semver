// SPDX-FileCopyrightText: Copyright © 2025 Caleb Cushing
//
// SPDX-License-Identifier: CC0-1.0

/** @type {import('prettier').Options} */
module.exports = {
  printWidth: 120,
  xmlWhitespaceSensitivity: "ignore",
  plugins: [
    require.resolve("@prettier/plugin-xml"),
    require.resolve("prettier-plugin-properties"),
    require.resolve("prettier-plugin-java"),
    require.resolve("prettier-plugin-toml"),
    require.resolve("prettier-plugin-sh"),
  ],
};
