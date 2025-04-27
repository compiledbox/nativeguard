Pod::Spec.new do |s|
    s.name         = 'nativeguard'
    s.version      = '1.0.0'
    s.summary      = 'NativeGuard: WireGuard VPN integration for React Native'
    s.license      = { :type => 'MIT', :file => 'LICENSE' }
    s.homepage     = 'https://github.com/compiledbox/nativeguard'
    s.authors      = { 'CompiledBox' => 'code@compiledbox.com' }
    s.platform     = :ios, '11.0'
    s.source       = { :path => '.' }
    s.source_files = 'Sources/**/*.{h,swift}'
    s.dependency   'React-Core'
  end
  