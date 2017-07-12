const path = require('path');
const webpack = require('webpack');
const ROOT = path.resolve(__dirname, 'src/main/resources/static');
const SRC = path.resolve(ROOT, 'javascript');
const DEST = path.resolve(__dirname, 'src/main/resources/static/dist');

module.exports = {
    devtool: 'source-map',
    entry: {
        app: SRC + '/index.jsx',
    },
    resolve: {
        root: [
            path.resolve(ROOT, 'javascript'),
            path.resolve(ROOT, 'css')
        ],
        extensions: ['', '.js', '.jsx']
    },
    output: {
        path: DEST,
        filename: 'bundle.js',
        publicPath: '/dist/'
    },
    module: {
        loaders: [
            {
                test: /\.jsx?$/,
                loaders: ['babel-loader?presets[]=es2015&presets[]=react'],
                include: SRC
            },

            {test: /\.css$/, loader: 'style-loader!css-loader'},
            {test: /\.less$/, loader: 'style!css!less'},

            // Needed for the css-loader when [bootstrap-webpack](https://github.com/bline/bootstrap-webpack)
            // loads bootstrap's css.
            {test: /\.(woff|woff2)(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?limit=10000&mimetype=application/font-woff'},
            {test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?limit=10000&mimetype=application/octet-stream'},
            {test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: 'file'},
            {test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?limit=10000&mimetype=image/svg+xml'}
        ]
    }
};