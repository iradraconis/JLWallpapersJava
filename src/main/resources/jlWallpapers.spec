%global _build_id_links none
%global _build_id_files %{nil}
%global debug_package %{nil}

#%{?elfbuildid}

%define __jar_repack %{nil}

Name: %{name}
Version: %{version}
Release: %{rpmrelease}
Summary: %{appdescription}
License: %{license}
URL: %{url}

%description
%{summary}

%files
%defattr(-,root,root,-)
%dir "%{appdir}"
"%{appdir}/%{executable}"
"%{appdir}/app/*"
%exclude /usr/lib/.build-id

%post
update-desktop-database &> /dev/null || :

%postun
if [ $1 -eq 0 ]; then
  update-desktop-database &> /dev/null || :
fi
