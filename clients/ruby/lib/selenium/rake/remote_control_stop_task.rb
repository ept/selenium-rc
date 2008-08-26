module Selenium
  module Rake
  
    class RemoteControlStopTask
      attr_accessor :host, :port, :timeout_in_seconds

      def initialize(name = :'selenium:rc:stop')
        @host = "localhost"
        @name = name
        @port = 4444
        @timeout_in_seconds = 5
        yield self if block_given?
        define
      end
    
      def define
        desc "Stop Selenium Remote Control running"
        task @name do
          puts "Stopping Selenium Remote Control running at #{@host}:#{@port}..."
          remote_control = Selenium::RemoteControl::RemoteControl.new(@host, @port, @timeout_in_seconds)
          remote_control.stop
          puts "Stopped Selenium Remote Control running at #{@host}:#{@port}"
        end
      end

    end
  end
end
