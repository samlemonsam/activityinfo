<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>${title} - ActivityInfo</title>

    <link rel="stylesheet" href="/about/assets/css/style.css">
    <link rel="icon" href="/about/assets/images/logo-activityinfo.png">

    <script type="text/javascript">
        var _gaq = _gaq || [];
        _gaq.push(['_setAccount', 'UA-11567120-1']);
        _gaq.push(['_setDomainName', 'activityinfo.org']);
        _gaq.push(['_trackPageview']);

        (function () {
            var ga = document.createElement('script');
            ga.type = 'text/javascript';
            ga.async = true;
            ga.src = 'https://ssl.google-analytics.com/ga.js';
            var s = document.getElementsByTagName('script')[0];
            s.parentNode.insertBefore(ga, s);
        })();
    </script>
</head>
<body>
<nav class="skiplinks">
    <a href="/login" class="visuallyhidden">Skip to login</a>
    <a href="#main" class="visuallyhidden">Skip to content</a>
    <a href="#footer" class="visuallyhidden">Skip to footer</a>
</nav>

<header role="banner">

    <nav role="navigation" id="navigation">
        <h1 class="logo"><img src="/about/assets/images/logo-activityinfo.png" alt=""><a href="/">ActivityInfo</a></h1>

        <ul>
            <li><a href="/about/benefits.html">Benefits</a></li>
            <li><a href="/about/features.html">Features</a></li>
            <li><a href="/about/pricing.html">Pricing</a></li>
            <li><a href="/blog/index.html">Blog</a></li>
            <li class="cta cta-signup"><a href="/signUp">Get Free Account</a></li>
            <li class="cta"><a href="/login">Log In</a></li>
        </ul>
    </nav>
</header>

<main role="main" id="main">
    ${body}
</main>


<footer class="bg-dark" id="footer">

    <nav class="row">
        <ul>
            <li>

                <h2>Product</h2>

                <ul>
                    <li><a href="/about/features.html">Features</a></li>
                    <li><a href="/about/benefits.html">Benefits</a></li>
                    <li><a href="/about/pricing.html">Pricing</a></li>
                    <li><a href="http://status.activityinfo.org">Status</a></li>
                    <li><a href="/about/releases.html">Releases</a></li>
                </ul>
            </li>
            <li>

                <h2>Resources</h2>

                <ul>
                    <li><a href="http://help.activityinfo.org">User Manual</a></li>
                    <li><a href="/apidocs/index.html">API Guide</a></li>
                    <li><a href="https://github.com/bedatadriven/activityinfo/blob/production/CONTRIBUTING.md">Contributor’s Guide</a></li>
                    <li><a href="/about/faq.html">FAQ</a></li>
                    <li><a href="/blog/index.html">Blog</a></li>
                    <li><a href="/video/index.html">Videos</a></li>
                </ul>
            </li>

            <li>

                <h2>ActivityInfo</h2>

                <ul>
                    <li><a href="/about/index.html">About Us</a></li>
                    <li><a href="/about/privacy-policy.html">Privacy Policy</a></li>
                    <li><a href="/about/terms.html">Terms of Service</a></li>
                    <li><a href="/about/contact.html">Contact</a></li>
                    <li>
                        <a href="https://twitter.com/activityinfo"><img src="/about/assets/images/twitter.svg" class="social-icon" alt="Twitter"></a>
                        <a href="https://www.linkedin.com/groups/5098257"><img src="/about/assets/images/linkedin.svg" class="social-icon" alt="LinkedIn"></a>
                        <a href="https://github.com/bedatadriven/activityinfo"><img src="/about/assets/images/github.svg" class="social-icon" alt="GitHub"></a>
                    </li>
                </ul>
            </li>

            <li>
                <img src="/about/assets/images/bdd-logo.svg" alt="BeDataDriven logo">
                <p>&copy; 2017 BeDataDriven</p>
            </li>
        </ul>
    </nav>
</footer>

</body>
</html>
