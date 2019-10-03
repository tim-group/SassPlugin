require 'rubygems'
require 'sass'

$mappings.each do |src, dst|
  engine = Sass::Engine.for_file(src, {:cache_location => $cacheLocation})
  css = engine.render
  File.open(dst, 'w') { |f| f.write(css) }
  puts "#{src} -> #{dst}"
end
