#!/usr/bin/perl
use strict;
use warnings;
use lib 'util';
use WWW::Selenium::XMLParseUtils qw/extract_functions html2pod 
                                    strip_blockquotes/;

my $iedoc = read_iedoc_xml();

# Convert HTML formatting into POD
$iedoc =~ s{^<\?.+?<top>}{#}s; # strip the xml header
$iedoc = html2pod($iedoc); 

$iedoc = strip_blockquotes($iedoc);

# remove extra newlines
$iedoc =~ s#\n{2,}#\n\n#g;

$iedoc =~ s#^(.+?)</top>##s;
my $selenium_description = $1;

my @functions = extract_functions($iedoc);

print perl_header(), 
      $selenium_description, 
      perl_constructor(), 
      @functions,
      perl_footer();

exit;

sub perl_header {
    return <<'EOT';
# Copyright 2006 ThoughtWorks, Inc
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#


package WWW::Selenium;
use LWP::UserAgent;
use HTTP::Request;
use URI::Escape;
use Carp qw(croak);

use strict;
use warnings;

our $VERSION = '0.27';

=head1 NAME

WWW::Selenium - Perl Client for the Selenium Remote Control test tool

=head1 SYNOPSIS

    use WWW::Selenium;
    
    my $sel = WWW::Selenium->new( host => "localhost", 
                                  port => 4444, 
                                  browser => "*iexplore", 
                                  browser_url => "http://www.google.com",
                                );
    
    $sel->start;
    $sel->open("http://www.google.com");
    $sel->type("q", "hello world");
    $sel->click("btnG");
    $sel->wait_for_page_to_load(5000);
    print $sel->get_title;
    $sel->stop;

=head1 DESCRIPTION

Selenium Remote Control (SRC) is a test tool that allows you to write
automated web application UI tests in any programming language against
any HTTP website using any mainstream JavaScript-enabled browser.  SRC
provides a Selenium Server, which can automatically start/stop/control
any supported browser. It works by using Selenium Core, a pure-HTML+JS
library that performs automated tasks in JavaScript; the Selenium
Server communicates directly with the browser using AJAX
(XmlHttpRequest).

L<http://www.openqa.org/selenium-rc/>

This module sends commands directly to the Server using simple HTTP
GET/POST requests.  Using this module together with the Selenium
Server, you can automatically control any supported browser.

To use this module, you need to have already downloaded and started
the Selenium Server.  (The Selenium Server is a Java application.)

=cut

### This next part is auto-generated based on the big comment in selenium-api.js

EOT
}

sub perl_constructor {
    return <<'EOT';

=cut

eval 'require Encode';
my $encode_present = !$@;
Encode->import('decode_utf8') if $encode_present;

sub new {
    my ($class, %args) = @_;
    my $self = { # default args:
                 host => 'localhost',
                 port => 4444,
                 browser_start_command => delete $args{browser} || '*firefox',
                 %args,
               };
    croak 'browser_url is mandatory!' unless $self->{browser_url};
    bless $self, $class or die "Can't bless $class: $!";
    return $self;
}

sub start {
    my $self = shift;
    $self->{session_id} = $self->get_string("getNewBrowserSession", 
                                            $self->{browser_start_command}, 
                                            $self->{browser_url});
}

sub stop {
    my $self = shift;
    $self->do_command("testComplete");
    $self->{session_id} = undef;
}

sub do_command {
    my ($self, $command, @args) = @_;
    $command = uri_escape($command);
    my $fullurl = "http://$self->{host}:$self->{port}/selenium-server/driver/"
                  . "\?cmd=$command";
    my $i = 1;
    @args = grep defined, @args;
    while (@args) {
        $fullurl .= "&$i=" . URI::Escape::uri_escape_utf8(shift @args);
        $i++;
    }
    if (defined $self->{session_id}) {
        $fullurl .= "&sessionId=$self->{session_id}";
    }
    print "---> Requesting $fullurl\n" if $self->{verbose};

    # We use the full version of LWP to make sure we issue an 
    # HTTP 1.1 request (SRC-25)
    my $ua = LWP::UserAgent->new;
    my $response = $ua->request( HTTP::Request->new(GET => $fullurl) );
    my $result;
    if ($response->is_success) {
        $result = $response->content;
        print "Got result: $result\n" if $self->{verbose};
    }
    else {
        die "Error requesting $fullurl:\n" . $response->status_line . "\n";
    }
    $result = decode_utf8($result) if $encode_present;
    die "Error requesting $fullurl:\n$result\n" unless $result =~ /^OK/;
    return $result;
}

sub get_string {
    my $self = shift;
    my $result = $self->do_command(@_);
    return substr($result, 3);
}

sub get_string_array {
    my $self = shift;
    my $result = $self->get_string(@_);
    my $token = "";
    my @tokens = ();
    my @chars = split(//, $result);
    for (my $i = 0; $i < @chars; $i++) {
        my $char = $chars[$i];
        if ($char eq '\\') {
            $i++;
            $char = $chars[$i];
            $token .= $char;
        } elsif ($char eq ',') {
            push (@tokens, $token);
            $token = "";
        } else {
            $token .= $char;
        }
    }
    push (@tokens, $token);
    return @tokens;
}

sub get_number {
    my $self = shift;
    my $result = $self->get_string(@_);
    # Is there something else I need to do here?
    return $result;
}

sub get_number_array {
    my $self = shift;
    my @result = $self->get_string_array(@_);
    # Is there something else I need to do here?
    return @result;
}

sub get_boolean {
    my $self = shift;
    my $result = $self->get_string(@_);
    if ($result eq "true") {
        return 1;
    }
    if ($result eq "false") {
        return 0;
    }
    die "result is neither 'true' nor 'false': $result";
}

sub get_boolean_array {
    my $self = shift;
    my @result = $self->get_string_array(@_);
    my @boolarr = ();
    for (my $i = 0; $i < @result; $i++) {
        if ($result[$i] eq "true") {
            push (@boolarr, 1);
            next;
        }
        if ($result[$i] eq "false") {
            push (@boolarr, 1);
            next;
        }
        die "result is neither 'true' nor 'false': ". $result[$i];
    }
    return @boolarr;
}

=head2 METHODS

The following methods are available:

=over

=item $sel = WWW::Selenium-E<gt>new( %args )

Constructs a new C<WWW::Selenium> object, specifying a Selenium Server
host/port, a command to launch the browser, and a starting URL for the
browser.

Options:

=over

=item * C<host>

host is the host name on which the Selenium Server resides.

=item * C<port>

port is the port on which the Selenium Server is listening.

=item * C<browser_url>

browser_url is the starting URL including just a domain name.  We'll
start the browser pointing at the Selenium resources on this URL,
e.g. "http://www.google.com" would send the browser to
"http://www.google.com/selenium-server/SeleneseRunner.html"

=item * C<browser> or C<browser_start_command>

This is the command string used to launch the browser, e.g.
"*firefox", "*iexplore" or "/usr/bin/firefox"

This option may be any one of the following:

=over

=item * C<*firefox [absolute path]>

Automatically launch a new Firefox process using a custom Firefox
profile.
This profile will be automatically configured to use the Selenium
Server as a proxy and to have all annoying prompts
("save your password?" "forms are insecure" "make Firefox your default
browser?" disabled.  You may optionally specify
an absolute path to your firefox executable, or just say "*firefox". 
If no absolute path is specified, we'll look for
firefox.exe in a default location (normally c:\program files\mozilla
firefox\firefox.exe), which you can override by
setting the Java system property C<firefoxDefaultPath> to the correct
path to Firefox.

=item * C<*iexplore [absolute path]>

Automatically launch a new Internet Explorer process using custom
Windows registry settings.
This process will be automatically configured to use the Selenium
Server as a proxy and to have all annoying prompts
("save your password?" "forms are insecure" "make Firefox your default
browser?" disabled.  You may optionally specify
an absolute path to your iexplore executable, or just say "*iexplore". 
If no absolute path is specified, we'll look for
iexplore.exe in a default location (normally c:\program files\internet
explorer\iexplore.exe), which you can override by
setting the Java system property C<iexploreDefaultPath> to the correct
path to Internet Explorer.

=item * C</path/to/my/browser [other arguments]>

You may also simply specify the absolute path to your browser
executable, or use a relative path to your executable (which we'll try
to find on your path).  B<Warning:> If you
specify your own custom browser, it's up to you to configure it
correctly.  At a minimum, you'll need to configure your
browser to use the Selenium Server as a proxy, and disable all
browser-specific prompting.

=back

=back

=cut

### From here on, everything's auto-generated from XML

EOT
}

sub perl_footer {
    return <<'EOT';
=pod

=back

=cut

1;

__END__

=head1 SEE ALSO

For more information about Selenium Remote Control, visit the website
at L<http://www.openqa.org/selenium-rc/>.

=head1 BUGS

The Selenium Remote Control JIRA issue tracking system is available
online at L<http://jira.openqa.org/browse/SRC>.

=head1 AUTHOR

Perl driver maintained by Luke Closs <selenium-rc@awesnob.com>

Selenium Remote Control maintained by Dan Fabulich <dfabulich@warpmail.net>

=head1 LICENSE

Copyright (c) 2006 ThoughtWorks, Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

EOT
}

sub read_iedoc_xml {
    my $iedoc_file = 'iedoc.xml';
    die "Can't find iedoc.xml" unless -e $iedoc_file;
    open (my $fh, $iedoc_file) or die "Can't open $iedoc_file: $!";
    {
        local $/ = undef;
        $iedoc = <$fh>;
    }
    close $fh;
    return $iedoc;
}